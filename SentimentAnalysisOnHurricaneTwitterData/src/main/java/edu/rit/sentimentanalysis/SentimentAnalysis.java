package edu.rit.sentimentanalysis;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.rit.constants.Constants;
import static edu.rit.util.ApplicationUtility.getProperties;
import java.util.Properties;
import org.bson.Document;

/**
 * Perform Sentiment Analysis for all clean tweets using Stanford CoreNLP.
 *
 * @author Harshit
 */
public class SentimentAnalysis {

    public static void main(String[] args) {
        // Connect Database
        Properties appProp = getProperties(Constants.AppConstants.APPLICATION_PROP_FILE_NAME);
        MongoClient mongo = new MongoClient(
                appProp.getProperty(Constants.AppConstants.HOST),
                Integer.parseInt(appProp.getProperty(Constants.AppConstants.PORT)));
        MongoDatabase db = mongo.getDatabase(appProp.getProperty(Constants.AppConstants.DATABASE));
        MongoCollection<Document> collection = db.getCollection(
                appProp.getProperty(Constants.AppConstants.CLEANED_COLLECTION));

        NLP.init();
        int sumScore = 0, size = 0;
        int positives = 0, negatives = 0;

        // Get all documents from collection
        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            String tweet = document.get(Constants.AppConstants.TWEET).toString();
            int sentimentScore = NLP.findSentiment(tweet);
            sumScore += sentimentScore;
            if (sentimentScore > 2) {
                positives++;
            }
            if (sentimentScore < 2) {
                negatives++;
            }
            System.out.println(tweet + " : " + sentimentScore);
            size++;
        }

        double avgScore = (double) sumScore / size;
        System.out.println("================================================");
        System.out.println("Average Sentiment Score: " + String.format("%.2f", avgScore) + " for total size: " + size);
        System.out.println("Positives: " + positives + "    Negatives: " + negatives + "    Neutrals: " + (size - positives - negatives));
    }
}
