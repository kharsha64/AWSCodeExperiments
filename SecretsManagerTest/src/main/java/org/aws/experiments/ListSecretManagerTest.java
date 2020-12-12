package org.aws.experiments;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Base64;
import java.util.List;

public class ListSecretManagerTest {

    static final Logger logger = LogManager.getLogger(ListSecretManagerTest.class);

    public static void main(String[] args) {
        // Replace with your Secret ARN before executing
        String secretName = "arn:aws:secretsmanager:ap-southeast-2:";
        String region = "ap-southeast-2";

        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();

        String secret = null;
        String decodedBinarySecret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        ListSecretsRequest listRequest = new ListSecretsRequest();
        Filter filter = new Filter();
        listRequest.withFilters();
        ListSecretsResult listSecretsResult = null;


        try {
             listSecretsResult = client.listSecrets(listRequest);

        } catch (DecryptionFailureException | InternalServiceErrorException | InvalidParameterException | InvalidRequestException | ResourceNotFoundException e) {

            /*
             * DecryptionFailureException - Secrets Manager can't decrypt the protected secret text using the provided KMS key
             * InternalServiceErrorException - An error occurred on the server side
             * InvalidParameterException - Provided an invalid value for a parameter
             * InvalidRequestException - Provided a parameter value that is not valid for the current state of the resource
             * ResourceNotFoundException - Can't find the resource
             */
            logger.error(e.getMessage());
            throw e;
        }

        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (listSecretsResult != null) {
            System.out.println("NOT NULL");
            List<SecretListEntry> secretList = listSecretsResult.getSecretList();
            for (SecretListEntry entry : secretList) {
                for (Tag tag : entry.getTags()) {
                    System.out.println("Key - " + tag.getKey() + " - Value - " + tag.getValue());
                }
            }
        } else {
            System.out.println("NULL");
        }

        System.out.println("Secret - " + secret);
        System.out.println("Decode Secret - " + decodedBinarySecret);
    }
}
