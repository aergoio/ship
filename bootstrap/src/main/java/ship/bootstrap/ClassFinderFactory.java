/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.File;
import lombok.Getter;
import lombok.Setter;

public class ClassFinderFactory implements Debuggable {

  @Getter
  @Setter
  protected boolean debug = false;

  /**
   * Create class finder for file.
   * <p>
   * Create JarClassFinder if file is general file.
   * Create DirectoryClassFinder if file is directory.
   * </p>
   *
   * @param file input file
   *
   * @return class finder for file
   */
  public ClassFinder create(final File file) {
    if (debug) {
      System.out.println("FILE: " + file.getAbsolutePath());
    }
    if (file.isDirectory()) {
      final DirectoryClassFinder finder = new DirectoryClassFinder(file);
      finder.setDebug(debug);
      return finder;
    } else if (file.isFile()) {
      final JarClassFinder finder = new JarClassFinder(file);
      finder.setDebug(debug);
      return finder;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
