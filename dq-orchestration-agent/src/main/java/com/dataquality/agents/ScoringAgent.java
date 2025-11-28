package com.dataquality.agents;

import com.dataquality.models.DataQualityMetric;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityScore;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ScoringAgent {
    
    private static final Logger logger = Logger.getLogger(ScoringAgent.class);
    
    @ConfigProperty(name = "data-quality.scoring.weights.completeness", defaultValue = "0.15")
    double completenessWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.uniqueness", defaultValue = "0.15")
    double uniquenessWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.consistency", defaultValue = "0.10")
    double consistencyWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.validity", defaultValue = "0.15")
    double validityWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.accuracy", defaultValue = "0.15")
    double accuracyWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.integrity", defaultValue = "0.10")
    double integrityWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.timeliness", defaultValue = "0.05")
    double timelinessWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.conformity", defaultValue = "0.05")
    double conformityWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.range", defaultValue = "0.05")
    double rangeWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.blanks", defaultValue = "0.03")
    double blanksWeight;
    
    @ConfigProperty(name = "data-quality.scoring.weights.outliers", defaultValue = "0.02")
    double outliersWeight;

    public DataQualityScore calculateScore(List<DataQualityResult> results) {
        logger.info("Calculating overall data quality score from " + results.size() + " results");
        
        Map<DataQualityMetric, Double> metricScores = new HashMap<>();
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (DataQualityResult result : results) {
            DataQualityMetric metric = result.getMetric();
            double score = result.getScore();
            double weight = getWeightForMetric(metric);
            
            metricScores.put(metric, score);
            weightedSum += score * weight;
            totalWeight += weight;
        }
        
        double overallScore = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        
        logger.info(String.format("Calculated overall score: %.3f", overallScore));
        
        return new DataQualityScore(overallScore, metricScores);
    }
    
    public double calculateImprovement(DataQualityScore preScore, DataQualityScore postScore) {
        if (preScore == null || postScore == null) {
            return 0.0;
        }
        
        double improvement = postScore.getOverallScore() - preScore.getOverallScore();
        logger.info(String.format("Data quality improvement: %.3f (from %.3f to %.3f)", 
            improvement, preScore.getOverallScore(), postScore.getOverallScore()));
        
        return improvement;
    }
    
    public Map<DataQualityMetric, Double> calculateMetricImprovements(DataQualityScore preScore, DataQualityScore postScore) {
        Map<DataQualityMetric, Double> improvements = new HashMap<>();
        
        if (preScore == null || postScore == null || 
            preScore.getMetricScores() == null || postScore.getMetricScores() == null) {
            return improvements;
        }
        
        for (DataQualityMetric metric : DataQualityMetric.values()) {
            Double preMetricScore = preScore.getMetricScores().get(metric);
            Double postMetricScore = postScore.getMetricScores().get(metric);
            
            if (preMetricScore != null && postMetricScore != null) {
                double improvement = postMetricScore - preMetricScore;
                improvements.put(metric, improvement);
            }
        }
        
        return improvements;
    }
    
    private double getWeightForMetric(DataQualityMetric metric) {
        return switch (metric) {
            case COMPLETENESS -> completenessWeight;
            case UNIQUENESS -> uniquenessWeight;
            case CONSISTENCY -> consistencyWeight;
            case VALIDITY -> validityWeight;
            case ACCURACY -> accuracyWeight;
            case INTEGRITY -> integrityWeight;
            case TIMELINESS -> timelinessWeight;
            case CONFORMITY -> conformityWeight;
            case RANGE -> rangeWeight;
            case BLANKS -> blanksWeight;
            case OUTLIERS -> outliersWeight;
        };
    }
}