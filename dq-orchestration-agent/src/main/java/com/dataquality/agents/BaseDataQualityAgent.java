package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseDataQualityAgent implements DataQualityAgent {
    
    protected final Logger logger = Logger.getLogger(getClass());
    
    @ConfigProperty(name = "data-quality.thresholds.completeness", defaultValue = "0.95")
    protected double completenessThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.uniqueness", defaultValue = "0.98")
    protected double uniquenessThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.consistency", defaultValue = "0.90")
    protected double consistencyThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.validity", defaultValue = "0.95")
    protected double validityThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.accuracy", defaultValue = "0.90")
    protected double accuracyThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.integrity", defaultValue = "0.95")
    protected double integrityThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.timeliness", defaultValue = "0.85")
    protected double timelinessThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.conformity", defaultValue = "0.90")
    protected double conformityThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.range", defaultValue = "0.95")
    protected double rangeThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.blanks", defaultValue = "0.05")
    protected double blanksThreshold;
    
    @ConfigProperty(name = "data-quality.thresholds.outliers", defaultValue = "0.10")
    protected double outliersThreshold;

    protected double getThresholdForMetric(DataQualityMetric metric) {
        return switch (metric) {
            case COMPLETENESS -> completenessThreshold;
            case UNIQUENESS -> uniquenessThreshold;
            case CONSISTENCY -> consistencyThreshold;
            case VALIDITY -> validityThreshold;
            case ACCURACY -> accuracyThreshold;
            case INTEGRITY -> integrityThreshold;
            case TIMELINESS -> timelinessThreshold;
            case CONFORMITY -> conformityThreshold;
            case RANGE -> rangeThreshold;
            case BLANKS -> blanksThreshold;
            case OUTLIERS -> outliersThreshold;
        };
    }

    protected DataQualityResult createResult(DataQualityMetric metric, double score, List<String> issues, List<String> recommendations, Map<String, Object> details) {
        double threshold = getThresholdForMetric(metric);
        DataQualityResult result = new DataQualityResult(metric, score, threshold);
        result.setIssues(issues != null ? issues : new ArrayList<>());
        result.setRecommendations(recommendations != null ? recommendations : new ArrayList<>());
        result.setDetails(details != null ? details : new HashMap<>());
        return result;
    }

    protected boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isBlank(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }

    @Override
    public String getAgentName() {
        return getClass().getSimpleName();
    }
}