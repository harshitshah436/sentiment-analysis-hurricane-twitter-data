package com.rit.hurricanetwitterdata;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import com.rit.constants.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Collect twitter data using Twitter Search API through Twitter4j library.
 * Query can be passed with date range and max_id to retrieve specific tweets.
 *
 * @author Harshit
 */
public class CollectTwitterData {

    private static final Logger LOGGER = Logger.getLogger(CollectTwitterData.class.getName());

    @SuppressWarnings({"SleepWhileInLoop", "LoggerStringConcat"})
    public static void main(String[] args) throws TwitterException, InterruptedException {

        // Enable raw json data storing to database.
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);
        TwitterFactory twitterFactory = new TwitterFactory(cb.build()); // The factory instance is re-useable and thread safe.
        Twitter twitter = twitterFactory.getInstance();

        // Set Twitter API keys.
        Properties twitterApiKeysProp = getProperties(Constants.AppConstants.TWITTER_PROP_FILE_NAME);
        AccessToken accessToken = new AccessToken(
                twitterApiKeysProp.getProperty(Constants.TwitterAPIConstants.ACCESS_TOKEN_KEY),
                twitterApiKeysProp.getProperty(Constants.TwitterAPIConstants.ACCESS_TOKEN_SECRET));
        twitter.setOAuthConsumer(
                twitterApiKeysProp.getProperty(Constants.TwitterAPIConstants.CONSUMER_KEY),
                twitterApiKeysProp.getProperty(Constants.TwitterAPIConstants.CONSUMER_SECRET));
        twitter.setOAuthAccessToken(accessToken);

        // Connect Database
        Properties appProp = getProperties(Constants.AppConstants.APPLICATION_PROP_FILE_NAME);
        MongoClient mongo = new MongoClient(
                appProp.getProperty(Constants.AppConstants.HOST),
                Integer.parseInt(appProp.getProperty(Constants.AppConstants.PORT)));
        MongoDatabase db = mongo.getDatabase(appProp.getProperty(Constants.AppConstants.DATABASE));
        MongoCollection<Document> collection = db.getCollection(
                appProp.getProperty(Constants.AppConstants.COLLECTION));

        // Set query properties to pass it to Twitter Search API
        Query query = new Query(appProp.getProperty(Constants.AppConstants.QUERY));
        query.setLang(Constants.AppConstants.LANG);
        query.setSince(appProp.getProperty(Constants.AppConstants.SINCE));
        query.setUntil(appProp.getProperty(Constants.AppConstants.UNTIL));
        Long maxId = Long.parseLong(appProp.getProperty(Constants.AppConstants.MAX_ID)) - 1;
        if (maxId > 0) {
            query.setMaxId(maxId);
            LOGGER.log(Level.INFO, "Max Id set: " + maxId);
        }
        LOGGER.log(Level.INFO, "Query: {0}", query.getQuery());

        QueryResult result = twitter.search(query);

        // In one try, only limited number of tweets are retrieved. So 
        // keep retrying until all tweets are received. 
        while (!result.getTweets().isEmpty()) {
            Long minId = Long.MAX_VALUE;
            Document document;
            for (Status status : result.getTweets()) {
                document = Document.parse(TwitterObjectFactory.getRawJSON(status));
                collection.insertOne(document);
                if (status.getId() < minId) {
                    minId = status.getId();
                }
                LOGGER.log(Level.INFO, "Id: " + status.getId());
            }
            query.setMaxId(minId - 1);
            try {
                result = searchWithRetry(twitter, query);
            } catch (TwitterException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                Thread.sleep(15 * 60 * 1000); // sleep for 15 minutes
                result = searchWithRetry(twitter, query);
            }
        }
    }

    /**
     * Try sending request again to Twitter API
     *
     * @param twitter TwitterFactory Instance
     * @param query query to perform
     * @return all retrieved tweets
     * @throws TwitterException
     */
    private static QueryResult searchWithRetry(Twitter twitter, Query query) throws TwitterException {
        return twitter.search(query);
    }

    /**
     * Load properties by given file name from resources folder.
     *
     * @param FILE_NAME file name
     * @return properties object containing all properties for given file name
     */
    public static Properties getProperties(String FILE_NAME) {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream(FILE_NAME)) {
            prop.load(resourceStream);
        } catch (IOException ex) {
            Logger.getLogger(CollectTwitterData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop;
    }
}
