package ship.util;

import java.io.Flushable;

public interface MessagePrinter extends Flushable {

  /**
   * Print message with {@code messageId} and {@code args}.
   *
   * @param messageId message id
   * @param args argument
   */
  void print(final String messageId, Object...args);

  /**
   * Insert empty line.
   */
  void println();

  /**
   * Print message with {@code messageId} and {@code args} and append line ending.
   *
   * @param messageId message id
   * @param args argument
   */
  void println(final String messageId, Object... args);
}
