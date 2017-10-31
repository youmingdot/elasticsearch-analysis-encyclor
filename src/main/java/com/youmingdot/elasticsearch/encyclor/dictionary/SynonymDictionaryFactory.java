package com.youmingdot.elasticsearch.encyclor.dictionary;

import com.youmingdot.elasticsearch.encyclor.EncyclorConfiguration;
import org.apache.lucene.analysis.Analyzer;

/**
 * Synonym Dictionary Factory
 *
 * @author You Ming
 */
public class SynonymDictionaryFactory {

    /**
     * Create a new synonym dictionary.
     *
     * @param configuration The configuration.
     *
     * @return The synonym dictionary.
     */
    public static SynonymDictionary createSynonymDictionary(EncyclorConfiguration configuration, Analyzer analyzer) {
        if (isRemoteUri(configuration.getUri())) {
            return new RemoteSynonymDictionary(configuration, analyzer);
        }

        return new LocalSynonymDictionary(configuration, analyzer);
    }

    private static boolean isRemoteUri(String uri) {
        return uri.startsWith("http://") || uri.startsWith("https://");
    }
}
