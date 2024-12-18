package com.dataincloud.api.configuration;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "production" })
public class BlobStorageConfiguration {
    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String connectionString;

    @Value("${spring.cloud.azure.storage.blob.container.users-profiles}")
    private String usersProfiles;

    @Value("${spring.cloud.azure.storage.blob.container.profiles-photos}")
    private String profilesPhotos;

    @Bean
    public String usersProfiles() {
        return usersProfiles;
    }

    @Bean
    public String profilesPhotos() {
        return profilesPhotos;
    }

    @Bean
    public BlobServiceClient blobServiceClient() {
        BlobServiceClient client = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        client.createBlobContainerIfNotExists(usersProfiles);
        client.createBlobContainerIfNotExists(profilesPhotos);

        return client;
    }
}
