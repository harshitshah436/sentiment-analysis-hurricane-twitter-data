package edu.rit.preprocessdata;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.rit.constants.Constants;
import static edu.rit.util.ApplicationUtility.getProperties;
import com.twitter.Extractor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 * Clean twitter data invoking from MongoDb and after cleaning insert into the
 * same Collection(Table).
 *
 * @author Harshit
 */
public class CleanTwitterData {

    private static final Logger LOGGER = Logger.getLogger(CleanTwitterData.class.getName());

    public static void main(String[] args) {

        // Connect Database
        Properties appProp = getProperties(Constants.AppConstants.APPLICATION_PROP_FILE_NAME);
        MongoClient mongo = new MongoClient(
                appProp.getProperty(Constants.AppConstants.HOST),
                Integer.parseInt(appProp.getProperty(Constants.AppConstants.PORT)));
        MongoDatabase db = mongo.getDatabase(appProp.getProperty(Constants.AppConstants.DATABASE));
        MongoCollection<Document> collection = db.getCollection(
                appProp.getProperty(Constants.AppConstants.CLEANED_COLLECTION));

        // Get all documents from collection
        FindIterable<Document> documents = collection.find();

        // Create a map of tweets after cleaning, so it will remove older duplicate tweets.
        HashMap<String, Document> cleanedDocumentsMap = new HashMap<>();
        for (Document document : documents) {
            String cleanedText = cleanTweet(document.get(Constants.AppConstants.TWEET).toString());
            document.replace(Constants.AppConstants.TWEET, cleanedText);
            // Insert first 60 chars of a string to check for a duplication
            String key = (cleanedText.length() > 60) ? cleanedText.substring(0, 61)
                    : cleanedText.substring(0, cleanedText.length());
            cleanedDocumentsMap.put(key, document);
        }

        // Drop initial collection and insert cleaned documents.
        collection.drop();
        LOGGER.log(Level.INFO, "Successfully dropped a collection.");

        collection.insertMany(new ArrayList<>(cleanedDocumentsMap.values()));
        LOGGER.log(Level.INFO, "Number of documentes inserted: {0}", cleanedDocumentsMap.size());
    }

    /**
     * Clean given tweet by removing URLs, associated usernames, punctuation.
     *
     * @param tweet
     * @return cleaned tweet.
     */
    private static String cleanTweet(String tweet) {
        Extractor extractor = new Extractor();

        // Remove URLs
        for (String url : extractor.extractURLs(tweet)) {
            tweet = tweet.replace(url, "");
        }

        // Remove Screen names or usernames
        for (String screenname : extractor.extractMentionedScreennames(tweet)) {
            tweet = tweet.replace(screenname, "");
        }

        //remove @ from the tweet which will be left after removing usernames
        tweet = tweet.replaceAll("@", "");

        //remove punctuation
        tweet = tweet.replaceAll("\\p{Punct}+", "");

        tweet = tweet.trim();

        return tweet;
    }
}
