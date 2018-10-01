package ship.util;

public class DummyMessagePrinter implements MessagePrinter {

  protected static DummyMessagePrinter instance = null;

  /**
   * Return singleton instance.
   *
   * @return singleton instance
   */
  public static MessagePrinter getInstance() {
    if (null == instance) {
      synchronized (DummyMessagePrinter.class) {
        if (null == instance) {
          instance = new DummyMessagePrinter();
        }
        return instance;
      }
    }
    return instance;
  }

  @Override
  public void print(String format, Object... args) {
  }

  @Override
  public void println() {
  }

  @Override
  public void println(String format, Object... args) {
  }

  @Override
  public void flush() {
  }
}
