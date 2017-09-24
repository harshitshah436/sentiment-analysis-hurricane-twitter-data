package edu.rit.constants;

/**
 * Application level constants.
 *
 * @author Harshit
 */
public final class Constants {

    private Constants() {
    } // prevent instantiation

    public static final class TwitterAPIConstants {

        private TwitterAPIConstants() {
        } // prevent instantiation

        public static final String ACCESS_TOKEN_KEY = "access_token_key";
        public static final String ACCESS_TOKEN_SECRET = "access_token_secret";
        public static final String CONSUMER_KEY = "consumer_key";
        public static final String CONSUMER_SECRET = "consumer_secret";
    }

    public static final class AppConstants {

        private AppConstants() {
        } // prevent instantiation

        public static final String TWITTER_PROP_FILE_NAME = "TwitterApiKeys.properties";
        public static final String APPLICATION_PROP_FILE_NAME = "AppResources.properties";
        public static final String STANFORD_CORENLP_PROP_FILE_NAME = "StanfordCoreNLP.properties";
        
        // Database Configuration
        public static final String HOST = "host";
        public static final String PORT = "port";
        public static final String DATABASE = "database";
        public static final String COLLECTION = "collection";

        // Twitter Search Query related constansts
        public static final String QUERY = "query";
        public static final String LANG = "en";
        public static final String SINCE = "since_date";
        public static final String UNTIL = "since_until";
        public static final String MAX_ID = "max_id";
        
        // Data Cleaning related constants
        public static final String CLEANED_COLLECTION = "cleanedcollection";
        public static final String TWEET = "text";
    }
}
