package org.aws.experiments;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Base64;

public class SecretManagerTest {

    static final Logger logger = LogManager.getLogger(SecretManagerTest.class);

    public static void main(String[] args) throws IOException {
        //String secretName = "DdApiKeySecret-lQ8hzWxNmW2m";
        String secretName = "arn:aws:secretsmanager:ap-southeast-2:";
        //String secretName = "aws:cloudformation:stack-name";
        String region = "ap-southeast-2";


        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        String secret = null;
        String decodedBinarySecret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
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
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(secret);
            System.out.println(json.get("TEST_KEY").toString());

        }
        else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }

        System.out.println("Secret - " + secret);
        System.out.println("Decode Secret - " + decodedBinarySecret);
    }
}
