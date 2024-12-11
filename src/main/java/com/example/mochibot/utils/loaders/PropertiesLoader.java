package com.example.mochibot.utils.loaders;

import com.example.mochibot.MochiBotApplication;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Properties;

public class PropertiesLoader {
    public static String loadProperties(String desiredProperty) {
        Properties properties = new Properties();

        try (InputStream input = MochiBotApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.printf("[%s] [ERROR] Unable to find application.properties", LocalTime.now());
            }
            properties.load(input);
        }
        catch (IOException e) {
            System.err.printf("[%s] [ERROR] Failed to load application.properties", LocalTime.now());
        }

        return properties.getProperty(desiredProperty);
    }
}
