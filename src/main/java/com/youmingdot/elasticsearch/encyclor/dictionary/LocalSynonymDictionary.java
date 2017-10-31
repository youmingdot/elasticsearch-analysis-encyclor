package com.youmingdot.elasticsearch.encyclor.dictionary;

import com.youmingdot.elasticsearch.encyclor.EncyclorConfiguration;
import com.youmingdot.elasticsearch.plugin.encyclor.EncyclorPlugin;
import org.apache.commons.codec.Charsets;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

/**
 * Local Synonym Dictionary
 *
 * @author You Ming
 */
public class LocalSynonymDictionary extends SynonymDictionary {

    private final static Logger LOGGER = ESLoggerFactory.getLogger(LocalSynonymDictionary.class);

    private final Path filePath;

    /**
     * Last modified time.
     */
    private long lastModified;

    public LocalSynonymDictionary(EncyclorConfiguration configuration, Analyzer analyzer) {
        super(configuration, analyzer);

        filePath = PathUtils.get(
                configuration.getEnvironment().configFile().toAbsolutePath().toString(),
                EncyclorPlugin.PLUGIN_NAME,
                configuration.getUri()
        );

        if (!filePath.toFile().exists()) {
            throw new IllegalArgumentException("The synonyms file " + filePath.toString() + " not found");
        }
    }

    @Override
    public String getUri() {
        return filePath.toString();
    }

    @Override
    public boolean isExpired() {
        try {
            File file = filePath.toFile();

            if (file.exists() && lastModified < file.lastModified()) {
                // Update last modified time.
                lastModified = filePath.toFile().lastModified();

                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while check file {} last modified time.", e, filePath.toString());
        }

        return false;
    }

    @Override
    protected Reader getSynonymReader() {
        Reader reader;

        try {
            reader = FileSystemUtils.newBufferedReader(filePath.toUri().toURL(), Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Read local synonyms file {} failed!", e, filePath.toString());

            throw new IllegalArgumentException("Read local synonyms file failed", e);
        }

        return reader;
    }
}
