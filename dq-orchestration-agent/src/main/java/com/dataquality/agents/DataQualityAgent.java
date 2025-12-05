package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;

public interface DataQualityAgent {
    
    /**
     * Gets the metric this agent is responsible for checking
     */
    DataQualityMetric getMetric();
    
    /**
     * Analyzes the dataset for the specific data quality metric
     */
    DataQualityResult analyze(Dataset dataset);
    
    /**
     * Attempts to rectify issues found in the dataset
     */
    Dataset rectify(Dataset dataset, DataQualityResult result);
    
    /**
     * Gets the agent's name/identifier
     */
    String getAgentName();
    
    /**
     * Checks if this agent can handle the given metric
     */
    default boolean canHandle(DataQualityMetric metric) {
        return getMetric().equals(metric);
    }
}