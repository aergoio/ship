package ship.build.web.service;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.Test;
import org.mockito.internal.verification.AtMost;
import ship.AbstractTestCase;

public class LiveUpdateServiceTest extends AbstractTestCase {
  @Test
  public void testRemove() throws IOException {
    final LiveUpdateSession session = mock(LiveUpdateSession.class);

    final LiveUpdateService liveUpdateService = new LiveUpdateService();
    liveUpdateService.add(session);
    liveUpdateService.remove(session);

    liveUpdateService.notifyChange(randomUUID().toString());
    verify(session, new AtMost(0)).sendMessage(anyString());
  }

  @Test
  public void testNotify() throws IOException {
    final LiveUpdateSession session = mock(LiveUpdateSession.class);

    final LiveUpdateService liveUpdateService = new LiveUpdateService();
    liveUpdateService.add(session);

    liveUpdateService.notifyChange(randomUUID().toString());
    verify(session).sendMessage(anyString());
  }

}