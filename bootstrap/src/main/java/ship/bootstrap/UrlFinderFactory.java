/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.File;
import lombok.Getter;
import lombok.Setter;

public class UrlFinderFactory implements Debuggable {

  @Getter
  @Setter
  protected boolean debug = false;

  /**
   * Create url finder for file.
   * <p>
   * Create {@link JarUrlFinder} if file is general file.
   * Create {@link DirectoryUrlFinder} if file is directory.
   * </p>
   *
   * @param file input file
   *
   * @return class finder for file
   */
  public UrlFinder create(final File file) {
    if (debug) {
      System.out.println("FILE: " + file.getAbsolutePath());
    }
    if (file.isDirectory()) {
      final DirectoryUrlFinder finder = new DirectoryUrlFinder(file);
      finder.setDebug(debug);
      return finder;
    } else if (file.isFile()) {
      final JarUrlFinder finder = new JarUrlFinder(file);
      finder.setDebug(debug);
      return finder;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
