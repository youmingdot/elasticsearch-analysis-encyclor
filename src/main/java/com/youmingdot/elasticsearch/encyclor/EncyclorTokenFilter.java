package com.youmingdot.elasticsearch.encyclor;

import com.youmingdot.elasticsearch.encyclor.dictionary.SynonymDictionary;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.elasticsearch.common.logging.ESLoggerFactory;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encyclor Token Filter
 *
 * @author You Ming
 */
public class EncyclorTokenFilter extends TokenFilter {

    private static final Logger LOGGER = ESLoggerFactory.getLogger(EncyclorTokenFilter.class);

    private final SynonymDictionary dictionary;
    private final Monitor monitor;

    private final EncyclorConfiguration configuration;

    private SynonymMap synonyms;
    private SynonymMap candidateSynonyms;

    private SynonymGraphFilter synonymFilter;

    public EncyclorTokenFilter(TokenStream input, SynonymDictionary dictionary, EncyclorConfiguration configuration) {
        super(input);

        this.dictionary = dictionary;
        this.configuration = configuration;

        monitor = new Monitor(this);

        initialSynonymMap();
    }

    /**
     * Initial the synonym map.
     */
    private void initialSynonymMap() {
        SynonymMap synonyms = dictionary.load();

        updateSynonymMap(synonyms);
        releaseCandidateSynonyms();

        monitor.run();
    }

    /**
     * Update the synonym map.
     *
     * @param synonyms The synonyms map
     */
    private void updateSynonymMap(SynonymMap synonyms) {
        if (synonyms.fst == null) {
            throw new IllegalArgumentException("Synonym map's fst must be non-null");
        }

        candidateSynonyms = synonyms;

        LOGGER.info("Synonyms dictionary is going to updating.");

        if (this.synonyms == null) {
            releaseCandidateSynonyms();
        }
    }

    /**
     * Make the candidate synonym map effective.
     */
    private void releaseCandidateSynonyms() {
        if (candidateSynonyms == null) {
            return;
        }

        synchronized (this) {
            if (candidateSynonyms != null) {
                synonyms = candidateSynonyms;
                synonymFilter = new SynonymGraphFilter(input, synonyms, configuration.isIgnoreCase());

                LOGGER.info("Synonyms dictionary has been updated.");

                candidateSynonyms = null;
            }
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        return synonymFilter.incrementToken();
    }

    @Override
    public void end() throws IOException {
        synonymFilter.end();
    }

    @Override
    public void close() throws IOException {
        synonymFilter.close();
    }

    @Override
    public void reset() throws IOException {
        synonymFilter.reset();

        releaseCandidateSynonyms();
    }

    static class Monitor implements Runnable {
        private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

        private final EncyclorTokenFilter filter;

        private static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);

                        thread.setDaemon(true);
                        thread.setName("encyclor-dictionary-monitor-" + ID_GENERATOR.addAndGet(1));

                        return thread;
                    }
                });

        Monitor(EncyclorTokenFilter filter) {
            this.filter = filter;
        }

        @Override
        public void run() {
            executorService.scheduleAtFixedRate(new MonitorTask(filter), 10, filter.configuration.getUpdateInterval(),
                    TimeUnit.SECONDS);
        }
    }

    static class MonitorTask implements Runnable {
        private final EncyclorTokenFilter filter;

        MonitorTask(EncyclorTokenFilter filter) {
            this.filter = filter;
        }

        @Override
        public void run() {
            SynonymDictionary dictionary = filter.dictionary;

            try {
                if (dictionary.isExpired()) {
                    // Reload synonym map.
                    SynonymMap synonyms = dictionary.load();

                    filter.updateSynonymMap(synonyms);
                }
            } catch (Exception e) {
                try {
                    LOGGER.error("Synchronizes the synonym dictionary {} failed.", dictionary.getUri());
                } catch (Exception ex) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
