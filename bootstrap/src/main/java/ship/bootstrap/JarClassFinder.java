/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class JarClassFinder implements ClassFinder, Debuggable {
  protected final File file;

  @Getter
  @Setter
  protected boolean debug = false;

  @Override
  public URL find(final String path) throws IOException {
    final JarFile jarFile = new JarFile(file);
    final JarEntry e = jarFile.getJarEntry(path);
    if (null == e) {
      return null;
    }
    if (e.isDirectory()) {
      return null;
    }

    if (debug) {
      System.out.println(path + " found");
    }

    final String canonicalEntryPath = (path.startsWith("/") ? path : ("/" + path));
    final String fullPath = "jar:" + file.toURI().toURL() + "!" + canonicalEntryPath;
    return new URL(fullPath);
  }

  @Override
  public String toString() {
    return "jar[" + file.getAbsolutePath() + "]";
  }
}
