package com.lukeludonglai.eventflow.database;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaEntityManagerFactory {

    private JpaEntityManagerFactory(){

    }

    public static EntityManagerFactory create(){
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                "jakarta.persistence.jdbc.url",
                requireEnvironmentVariable("DB_URL")
        );

        properties.put(
                "jakarta.persistence.jdbc.user",
                requireEnvironmentVariable("DB_USER")
        );

        properties.put(
                "jakarta.persistence.jdbc.password",
                requireEnvironmentVariable("DB_PASSWORD")
        );

        properties.put(
                "jakarta.persistence.jdbc.driver",
                "org.postgresql.Driver"
        );

        properties.put(
                "hibernate.hbm2ddl.auto",
                "create"
        );

        properties.put(
                "hibernate.show_sql",
                "true"
        );

        properties.put(
                "hibernate.format_sql",
                "true"
        );

        return Persistence.createEntityManagerFactory(
                "eventflow",
                properties
        );
    }

    private static String requireEnvironmentVariable(String name){
        String value = System.getenv(name);

        if (value == null || value.isBlank()){
            throw new IllegalStateException("Missing environment variable: " + name);
        }

        return value;
    }
}
