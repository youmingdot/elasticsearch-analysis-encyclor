package com.youmingdot.elasticsearch.plugin.encyclor;

import com.youmingdot.elasticsearch.encyclor.EncyclorTokenFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Encyclor Plugin
 *
 * @author You Ming
 */
public class EncyclorPlugin extends Plugin implements AnalysisPlugin {

    public static final String PLUGIN_NAME = "analysis-encyclor";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new HashMap<>();

        extra.put("encyclor", EncyclorTokenFilterFactory::getEncyclorTokenFilterFactory);

        return extra;
    }
}
