/*
 * @copyright defined in LICENSE.txt
 */

package ship.ship.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class IoUtils {

  /**
   * Read all bytes from in and return byte[].
   *
   * @param in input stream to read
   * @param size hint to optimize the reading
   *
   * @return all bytes in stream
   *
   * @throws IOException if fail to read from stream
   */
  public static byte[] readFully(final InputStream in, final long size) throws IOException {
    final int bufferSize = (Integer.MAX_VALUE < size) ? Integer.MAX_VALUE : (int) size;
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream(bufferSize);
    final byte[] buffer = new byte[bufferSize];
    int readBytes;
    while (0 < (readBytes = in.read(buffer))) {
      byteOut.write(buffer, 0, readBytes);
    }
    return byteOut.toByteArray();
  }

  /**
   * Rake files from directory and its subdirectory.
   *
   * @param dir string directory
   *
   * @return all files under dir
   */
  public static Stream<File> rake(final File dir) {
    if (dir.isDirectory()) {
      return Arrays.stream(Objects.requireNonNull(dir.listFiles())).flatMap(IoUtils::rake);
    } else {
      return Stream.of(dir);
    }
  }

}
