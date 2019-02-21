/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ship.util.DangerousSupplier;

@RequiredArgsConstructor
public class FileContent implements Comparable<FileContent> {
  @Getter
  @NonNull
  protected final String path;

  protected final DangerousSupplier<InputStream> contentSupplier;

  /**
   * Open stream for content.
   *
   * @return input stream
   *
   * @throws IOException Fail to access I/O
   */
  public InputStream open() throws IOException {
    try {
      return contentSupplier.get();
    } catch (final IOException ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + path + "]";
  }

  @Override
  public int compareTo(final FileContent o) {
    return this.path.compareTo(o.path);
  }

  public File getFileFrom(File base) {
    return new File(base, path);
  }
}
