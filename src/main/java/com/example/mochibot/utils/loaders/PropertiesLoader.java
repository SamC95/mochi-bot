package com.example.mochibot.utils.loaders;

import com.example.mochibot.MochiBotApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static String loadProperties(String desiredProperty) {
        Properties properties = new Properties();

        try (InputStream input = MochiBotApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Error retrieving application.properties");
            }
            properties.load(input);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        return properties.getProperty(desiredProperty);
    }
}
