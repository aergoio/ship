package ship.util;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {

  /**
   * Try to close obj without any exception.
   *
   * @param obj object to close
   */
  public static void tryClose(final Object obj) {
    try {
      if (obj instanceof InputStream) {
        ((InputStream) obj).close();
      } else if (obj instanceof OutputStream) {
        ((InputStream) obj).close();
      } else if (obj instanceof Closeable) {
        ((Closeable) obj).close();
      }
    } catch (final Throwable ex) {
      ;// Ignore exception
    }
  }

}
