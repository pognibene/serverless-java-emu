package com.agileandmore.emulator.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDbManager {
    /**
     * Get a dynamodb mapper configured properly for either lcoal testing
     * or the default URL or a custom URL (for end to end testing)
     *
     * @return
     */
    public static
    DynamoDB getMapper() {
        String dynamoUrl = System.getProperty("dynamodb.url");
        String dynamoRegion = System.getProperty("dynamodb.region");

        AmazonDynamoDB client;

        // non empty URL : local database with custom port/url
        if (dynamoUrl != null && dynamoUrl.trim().length() != 0) {
            // setup fake credentials for local usage
            System.setProperty("aws.accessKeyId", "super-access-key");
            System.setProperty("aws.secretKey", "super-secret-key");

            client = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(dynamoUrl, "us-west-2"))
                    .build();
        } else {
            // empty url and no region : local database with default url
            if (dynamoRegion == null || dynamoRegion.trim().length() == 0) {
                System.setProperty("aws.accessKeyId", "super-access-key");
                System.setProperty("aws.secretKey", "super-secret-key");

                client = AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                        .build();
            }
            // empty url and non empty region : remote database
            // with default url but custom region
            else {
                client = AmazonDynamoDBClientBuilder.standard().withRegion(dynamoRegion).build();
            }
        }
        DynamoDB dynamoDB = new DynamoDB(client);
        return dynamoDB;
    }

    /**
     * Clear the content of all tables in the specified dynamodb instance.
     * This does not destroy the tables, only iterates then clean the content.
     * This operation is meant to be used with small amount of data in the scope of *tests*
     * (max one thousand rows maybe)
     */
//    public static void clearAllTables(DynamoDBMapper mapper) {
//
//    }
}
