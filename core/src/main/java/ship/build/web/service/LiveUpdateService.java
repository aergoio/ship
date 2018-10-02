/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;

/**
 * LiveUpdateSession holder.
 */
@Named
public class LiveUpdateService extends AbstractService {

  /**
   * Holding sessions.
   */
  protected final Set<LiveUpdateSession> sessions = new HashSet<>();

  /**
   * Add session to managed area.
   *
   * @param liveUpdateSession session to add
   */
  public void add(final LiveUpdateSession liveUpdateSession) {
    sessions.add(liveUpdateSession);
    logger.info("{} created", liveUpdateSession);
  }

  /**
   * Remove session from managed area.
   *
   * @param liveUpdateSession session to remove
   */
  public void remove(final LiveUpdateSession liveUpdateSession) {
    sessions.remove(liveUpdateSession);
    logger.info("{} removed", liveUpdateSession);
  }

  /**
   * Push message to connected client via websocket.
   *
   * @param message message to send
   *
   * @throws IOException Fail to send
   */
  public void notifyChange(final Object message) throws IOException {
    final String text = new ObjectMapper().writeValueAsString(message);
    logger.info("{} receiver(s)", sessions.size());
    for (final LiveUpdateSession session : sessions) {
      try {
        session.sendMessage(text);
      } catch (final IOException e) {
        logger.debug("Unexpected exception:", e);
      }
    }
  }
}
