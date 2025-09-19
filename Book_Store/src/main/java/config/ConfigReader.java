package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    public static void setUp()
    {
        String configFilePath="src/test/envConfiguration/application-QA.properties";
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(configFilePath)) {
            if (input == null) {
                throw new RuntimeException("Properties file not found: " + configFilePath);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + configFilePath, e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
