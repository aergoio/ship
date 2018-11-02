package ship.util;

import java.io.Flushable;

public interface MessagePrinter extends Flushable {
  void print(final String messageId, Object...args);

  void println();

  void println(final String messageId, Object... args);
}
