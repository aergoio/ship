package ship.util;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ship.AbstractTestCase;
import ship.exception.DirectoryNotEmptyException;

public class MessagesTest extends AbstractTestCase {

  @Test
  public void testBind() {
    final String messageId = DirectoryNotEmptyException.class.getName() + ".0";
    final String uuid = randomUUID().toString();
    final String message = Messages.bind(messageId, uuid);
    logger.debug("Message: {}", message);
    assertNotNull(message);
    assertTrue(message.contains(uuid));
    assertFalse(message.contains(messageId));
  }

  @Test
  public void shouldNotThrowException() {
    final String message = Messages.bind(randomUUID().toString(), randomUUID());
    logger.debug("Message: {}", message);
    assertNotNull(message);
  }

  @Test
  public void shouldKeepSpace() {
    final String message = Messages.bind("ship.command.TestProject.2", randomUUID());
    logger.debug("Message: {}", message);
    assertTrue(message.startsWith(" "));
  }

}