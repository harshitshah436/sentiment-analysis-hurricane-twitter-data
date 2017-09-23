package com.rit.util;

import com.rit.hurricanetwitterdata.CollectTwitterData;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application level utility class to provide utility methods.
 *
 * @author Harshit
 */
public class ApplicationUtility {

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
