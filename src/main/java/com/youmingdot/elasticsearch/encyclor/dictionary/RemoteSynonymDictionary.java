package com.youmingdot.elasticsearch.encyclor.dictionary;

import com.youmingdot.elasticsearch.encyclor.EncyclorConfiguration;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.Reader;

/**
 * Remote Synonym Dictionary
 *
 * @author You Ming
 */
public class RemoteSynonymDictionary extends SynonymDictionary {

    private final static Logger LOGGER = ESLoggerFactory.getLogger(RemoteSynonymDictionary.class);

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Http Last-Modified.
     */
    private String lastModified;

    /**
     * Http ETag.
     */
    private String eTag;

    public RemoteSynonymDictionary(EncyclorConfiguration configuration, Analyzer analyzer) {
        super(configuration, analyzer);
    }

    @Override
    public boolean isExpired() {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000)
                .build();

        HttpHead head = new HttpHead(configuration.getUri());
        head.setConfig(config);

        // Set the request header
        if (this.lastModified != null) {
            head.setHeader("If-Modified-Since", this.lastModified);
        }

        if (this.eTag != null) {
            head.setHeader("If-None-Match", this.eTag);
        }

        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(head);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                Header lastModified = response.getLastHeader("Last-Modified");
                Header eTag = response.getLastHeader("ETag");

                if ((lastModified != null && !lastModified.getValue().equalsIgnoreCase(this.lastModified))
                        || (eTag != null && !eTag.getValue().equalsIgnoreCase(this.eTag))) {
                    // Update last modified info.
                    this.lastModified = lastModified != null ? lastModified.getValue() : this.lastModified;
                    this.eTag = eTag != null ? eTag.getValue() : this.eTag;

                    return true;
                }
            } else if (statusCode == 304) {
                // Http 304 : Not Modified
            } else {
                LOGGER.info("Remote url {} return bad code {}" , configuration.getUri(), statusCode);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while check remote url {} last modified time.", e, configuration.getUri());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                LOGGER.error("Operate failed while close Http response.", e);
            }
        }

        return false;
    }

    @Override
    protected Reader getSynonymReader() {
        return null;
    }
}
