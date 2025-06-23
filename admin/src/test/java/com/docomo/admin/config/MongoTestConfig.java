package com.docomo.admin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@TestConfiguration
public class MongoTestConfig {

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoConverter converter = new MappingMongoConverter(
            new DefaultDbRefResolver(mongoClient.getDatabase("test")),
            new MongoMappingContext()
        );
        ((MappingMongoConverter) converter).setTypeMapper(new DefaultMongoTypeMapper(null));
        ((MappingMongoConverter) converter).setCustomConversions(mongoCustomConversions());
        
        return new MongoTemplate(mongoClient, "test");
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(java.util.Collections.emptyList());
    }
} 