package edu.rit.sentimentanalysis;

import edu.rit.constants.Constants;
import static edu.rit.util.ApplicationUtility.getProperties;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.List;

/**
 * This class is mainly for performing NLP techniques and sending back result to
 * calling class. In our case, it will calculate sentiment score for the given
 * tweet.
 *
 * @author Harshit
 */
public class NLP {

    static StanfordCoreNLP pipeline;

    public static void init() {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties prop = getProperties(Constants.AppConstants.STANFORD_CORENLP_PROP_FILE_NAME);
        pipeline = new StanfordCoreNLP(prop);
    }

    public static int findSentiment(String tweet) {
        int sentiment = -1;
        if (tweet != null && tweet.length() > 0) {

            // create an empty Annotation just with the given text
            Annotation document = new Annotation(tweet);

            // run all Annotators on this text
            pipeline.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                    // this is the text of the token
                    String word = token.get(TextAnnotation.class);
                    // this is the POS tag of the token
                    String pos = token.get(PartOfSpeechAnnotation.class);
                    // this is the NER label of the token
                    String ne = token.get(NamedEntityTagAnnotation.class);
                    System.out.println("Token: " + word + " POS: " + pos + " Name Entity: " + ne);
                }
                Tree tree = sentence.get(SentimentAnnotatedTree.class);
                sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String sentiment_new = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                System.out.println("sentence:  " + sentence + " : " + sentiment + "  " + sentiment_new);
            }
        }
        return sentiment;
    }
}
