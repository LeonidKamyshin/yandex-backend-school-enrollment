package com.yandex.enrollment.api.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

  @Value("${spring.data.mongodb.username}")
  private String username;

  @Value("${spring.data.mongodb.password}")
  private String password;

  @Value("${spring.data.mongodb.authentication-database}")
  private String authDatabase;

  @Value("${spring.data.mongodb.database}")
  private String database;

  @Bean
  public MongoClient mongo() {
    ConnectionString connectionString = new ConnectionString(
        "mongodb://localhost:27017/" + database);
    MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .credential(
            MongoCredential.createCredential(username, authDatabase, password.toCharArray()))
        .build();

    return MongoClients.create(mongoClientSettings);
  }

  @Bean
  public MongoTemplate mongoTemplate() {
    return new MongoTemplate(mongo(), database);
  }
}
