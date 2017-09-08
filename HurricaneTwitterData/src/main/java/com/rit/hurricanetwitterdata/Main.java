package com.rit.hurricanetwitterdata;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
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

/**
 *
 * @author Harshit
 */
public class Main {

    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) throws TwitterException, InterruptedException {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);

        // The factory instance is re-useable and thread safe.
        TwitterFactory twitterFactory = new TwitterFactory(cb.build());
        Twitter twitter = twitterFactory.getInstance();

        AccessToken accessToken = new AccessToken("Access Token", "Access Token Secret");
        twitter.setOAuthConsumer("Consumer Key", "Consumer Secret");
        twitter.setOAuthAccessToken(accessToken);

        Query query = new Query("hurricane irma -filter:retweets");
        query.setLang("en");
        // set the bounding dates 
        query.setSince("2017-09-06");
        query.setUntil("2017-09-07");
        System.out.println("Query: " + query.getQuery());

        MongoClient mongo = new MongoClient("localhost", 27017);
        MongoDatabase db = mongo.getDatabase("hurricanetweets");
        MongoCollection<Document> collection = db.getCollection("irma_tweets_all");

        Document doc = collection.find(eq("id_str", "905553500347576320")).first();
        System.out.println("Max Id set: " + ((long) doc.get("id") - 1));
        query.setMaxId((long) doc.get("id") - 1);

        QueryResult result = twitter.search(query);

        while (!result.getTweets().isEmpty()) {
            Long minId = Long.MAX_VALUE;
            Document document;
            for (Status status : result.getTweets()) {
                document = Document.parse(TwitterObjectFactory.getRawJSON(status));
                collection.insertOne(document);
                if (status.getId() < minId) {
                    minId = status.getId();
                }
                System.out.println("Id: " + status.getId());
            }
            query.setMaxId(minId - 1);
            try {
                result = searchWithRetry(twitter, query);
            } catch (TwitterException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                Thread.sleep(15 * 60 * 1000); // sleep for 15 minutes
                result = searchWithRetry(twitter, query);
            }
        }
    }

    private static QueryResult searchWithRetry(Twitter twitter, Query query) throws TwitterException {
        return twitter.search(query);
    }
}
