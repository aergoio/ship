package ship.build.web.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashSet;
import org.junit.Test;
import org.springframework.web.socket.WebSocketSession;
import ship.AbstractTestCase;

public class LiveUpdateSessionTest extends AbstractTestCase {

  @Test
  public void testSendMessage() throws IOException {
    final WebSocketSession webSocketSession = mock(WebSocketSession.class);
    final LiveUpdateSession session = new LiveUpdateSession(webSocketSession);
    session.sendMessage(randomUUID().toString());
    verify(webSocketSession).sendMessage(any());
  }

  @Test
  public void testHashCodeAndEquals() {
    final WebSocketSession webSocketSession = mock(WebSocketSession.class);
    final LiveUpdateSession session1 = new LiveUpdateSession(webSocketSession);
    final LiveUpdateSession session2 = new LiveUpdateSession(webSocketSession);

    assertTrue(session1.equals(session2));
    assertTrue(new HashSet(asList(session1)).contains(session2));
  }
}