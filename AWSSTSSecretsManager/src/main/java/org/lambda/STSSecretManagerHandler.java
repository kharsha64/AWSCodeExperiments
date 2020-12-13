package org.lambda;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsync;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class STSSecretManagerHandler implements RequestHandler<String, String>{

    static final Logger logger = LogManager.getLogger(STSSecretManagerHandler.class);

    // Replace with the correct secret ARN
    private final String SECRET_ARN = "arn:aws:secretsmanager:ap-southeast-2:XXXXXXX";
    private final String REGION_NAME = "ap-southeast-2";

    private final String STS_ENDPOINT = "sts.ap-southeast-2.amazonaws.com";

    // Replace with the correct assume role ARN
    private final String ROLE_ARN="arn:aws:iam::XXXXXXXX";
    private final String ROLE_SESSION_NAME="cross_acct_lambda";

    @Override
    public String handleRequest(String s, Context context) {

        AWSSecurityTokenServiceAsync stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(STS_ENDPOINT, REGION_NAME)).build();

        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest().withRoleArn(ROLE_ARN).withRoleSessionName(ROLE_SESSION_NAME);

        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
        Credentials credentials = assumeRoleResult.getCredentials();
        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(credentials.getAccessKeyId(), credentials.getSecretAccessKey(), credentials.getSessionToken());

        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(sessionCredentials)).build();

        String secret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(SECRET_ARN);
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
            logger.error(e.getMessage(), e);
            throw e;
        }

        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = null;
            try {
                json = mapper.readTree(secret);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }


            logger.debug("Secret JSON : " + json.toString());

            logger.debug("JDBC_RDBMS - " + json.get("engine").textValue());

            logger.debug("JDBC_HOST - " + json.get("host").textValue());
            logger.debug("JDBC_DBNAME - " + json.get("dbname").textValue());
            logger.debug("JDBC_USER - " + json.get("username").textValue());
            logger.debug("JDBC_PWD - " + json.get("password").textValue());
            logger.debug("JDBC_PORT - " + json.get("port").asInt());


        }


        return secret;
    }


}
