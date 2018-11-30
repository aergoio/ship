package ship.util;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * This is object to bind message for i18n.
 */
public class Messages {

  protected static Properties messages;

  static {
    messages = new Properties();
    try (final InputStream in = Messages.class.getResourceAsStream("/message.properties")) {
      messages.load(in);
    } catch (final IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static boolean exists(final String msgId) {
    return null != getMessage(msgId);
  }

  public static String getMessage(final String msgId) {
    return messages.getProperty(msgId);
  }

  /**
   * Bind message with message id and arguments.
   *
   * @param msgId message id
   * @param args arguments
   *
   * @return bound message
   */
  public static String bind(final String msgId, final Object... args) {
    final String pattern = getMessage(msgId);
    if (null == pattern) {
      final StringJoiner joiner = new StringJoiner(",");
      stream(args).map(Object::toString).forEach(joiner::add);
      return "!!" + msgId + "!!: " + joiner.toString();
    }
    return MessageFormat.format(pattern, args);
  }

}
