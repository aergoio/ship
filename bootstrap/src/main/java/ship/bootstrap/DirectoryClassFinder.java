/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class DirectoryClassFinder implements ClassFinder, Debuggable {
  protected final File baseDir;

  @Getter
  @Setter
  protected boolean debug = false;

  @Override
  public URL find(String path) throws IOException {
    final File f = new File(baseDir, path);
    if (f.exists() && f.isFile() && f.canRead()) {
      return f.toURI().toURL();
    }
    return null;
  }

  @Override
  public String toString() {
    return "dir[" + baseDir.getAbsolutePath() + "]";
  }
}
