/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class DirectoryUrlFinder implements UrlFinder, Debuggable {
  protected final File baseDir;

  @Getter
  @Setter
  protected boolean debug = false;

  @Override
  public List<URL> findUrls(final String path) throws IOException {
    final File f = new File(baseDir, path);
    if (f.exists() && f.isFile() && f.canRead()) {
      if (debug) {
        System.out.println("DirectoryClassFinder: " + path + " found");
      }
      final URL url = f.toURI().toURL();
      final List<URL> urls = new ArrayList<URL>();
      urls.add(url);
      return urls;
    }
    return emptyList();
  }

  @Override
  public String toString() {
    return "dir[" + baseDir.getAbsolutePath() + "]";
  }
}
