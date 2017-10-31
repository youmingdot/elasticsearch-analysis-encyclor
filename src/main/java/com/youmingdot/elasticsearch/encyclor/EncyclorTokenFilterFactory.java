package com.youmingdot.elasticsearch.encyclor;

import com.youmingdot.elasticsearch.encyclor.dictionary.SynonymDictionary;
import com.youmingdot.elasticsearch.encyclor.dictionary.SynonymDictionaryFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Encyclor Token Filter Factory
 *
 * @author You Ming
 */
public class EncyclorTokenFilterFactory extends AbstractTokenFilterFactory {

    private final EncyclorConfiguration configuration;

    private final Analyzer analyzer;

    public EncyclorTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
            Settings settings) {
        super(indexSettings, name, settings);

        configuration = new EncyclorConfiguration(settings, environment);

        analyzer = createTokenAnalyzer(configuration);
    }

    public static EncyclorTokenFilterFactory getEncyclorTokenFilterFactory(IndexSettings indexSettings,
            Environment environment, String name, Settings settings) {
        return new EncyclorTokenFilterFactory(indexSettings, environment, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        SynonymDictionary dictionary = SynonymDictionaryFactory.createSynonymDictionary(configuration, analyzer);

        return new EncyclorTokenFilter(tokenStream, dictionary, configuration);
    }

    private Analyzer createTokenAnalyzer(EncyclorConfiguration configuration) {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new WhitespaceTokenizer();

                TokenStream stream = configuration.isIgnoreCase() ? new LowerCaseFilter(tokenizer) : tokenizer;

                return new TokenStreamComponents(tokenizer, stream);
            }
        };
    }
}
