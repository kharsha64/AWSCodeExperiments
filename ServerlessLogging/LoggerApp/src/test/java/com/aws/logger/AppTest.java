package com.aws.logger;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
  @Test
  public void successfulResponse() {
    LogLevelTest app = new LogLevelTest();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
    assertEquals(result.getStatusCode().intValue(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains("\"message\""));
    assertTrue(content.contains("\"hello world\""));
    assertTrue(content.contains("\"location\""));
  }
}
