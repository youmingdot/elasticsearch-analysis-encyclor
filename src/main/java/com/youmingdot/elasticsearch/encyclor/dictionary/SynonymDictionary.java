package com.youmingdot.elasticsearch.encyclor.dictionary;

import com.youmingdot.elasticsearch.encyclor.EncyclorConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.Reader;

/**
 * Synonym Dictionary
 *
 * @author You Ming
 */
public abstract class SynonymDictionary {

    private static final Logger LOGGER = ESLoggerFactory.getLogger(SynonymDictionary.class);

    private static final String FORMAT_WORDNET = "wordnet";

    protected final EncyclorConfiguration configuration;

    protected final Analyzer analyzer;

    public SynonymDictionary(EncyclorConfiguration configuration, Analyzer analyzer) {
        this.configuration = configuration;
        this.analyzer = analyzer;
    }

    /**
     * Get the URI for the dictionary.
     *
     * @return The URI.
     */
    public String getUri() {
        return configuration.getUri();
    }

    /**
     * Determine whether the dictionary is out of expired.
     *
     * @return Return true if the dictionary need to update.
     */
    abstract public boolean isExpired();

    /**
     * Load the synonym map from dictionary.
     *
     * @return The synonym map.
     */
    public SynonymMap load() {
        return parseSynonymMap(getSynonymReader());
    }

    /**
     * Parse synonym map from reader.
     *
     * @param reader The reader.
     *
     * @return The synonym map.
     */
    protected SynonymMap parseSynonymMap(Reader reader) {
        try {
            SynonymMap.Parser parser;

            if (FORMAT_WORDNET.equalsIgnoreCase(configuration.getFormat())) {
                parser = new WordnetSynonymParser(true, configuration.isExpand(), analyzer);
            } else {
                parser = new SolrSynonymParser(true, configuration.isExpand(), analyzer);
            }

            parser.parse(reader);

            return parser.build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Synonyms file cannot be parsed", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close reader.", e);
            }
        }
    }

    /**
     * Get the synonym's rules reader.
     *
     * @return The reader.
     */
    abstract protected Reader getSynonymReader();
}
