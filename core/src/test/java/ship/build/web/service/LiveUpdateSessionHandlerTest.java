package ship.build.web.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import ship.AbstractTestCase;

public class LiveUpdateSessionHandlerTest extends AbstractTestCase {
  @Test
  public void testAfterConnectionEstablished() throws Exception {
    // Given
    final LiveUpdateService manager = mock(LiveUpdateService.class);
    final WebSocketSession session = mock(WebSocketSession.class);

    final LiveUpdateSessionHandler handler = new LiveUpdateSessionHandler();
    handler.setManager(manager);

    // When
    handler.afterConnectionEstablished(session);

    // Then
    verify(manager).add(any());
  }

  @Test
  public void testAfterConnectionClosed() throws Exception {
    // Given
    final LiveUpdateService manager = mock(LiveUpdateService.class);
    final WebSocketSession session = mock(WebSocketSession.class);

    final LiveUpdateSessionHandler handler = new LiveUpdateSessionHandler();
    handler.setManager(manager);

    // When
    handler.afterConnectionClosed(session, CloseStatus.NORMAL);

    // Then
    verify(manager).remove(any());
  }

}