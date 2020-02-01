/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class JarUrlFinder implements UrlFinder, Debuggable {
  protected final File file;

  @Getter
  @Setter
  protected boolean debug = false;

  protected HashSet<String> indices = null;

  @Override
  public List<URL> findUrls(final String path) throws IOException {
    try (final JarFile jarFile = new JarFile(file)) {
      final List<URL> urls = new ArrayList<>();

      final JarEntry e = jarFile.getJarEntry(path);
      if (null == e) {
        return emptyList();
      }

      if (e.isDirectory()) {
        final Enumeration<JarEntry> it = jarFile.entries();
        while (it.hasMoreElements()) {
          final JarEntry next = it.nextElement();
          if (next.getName().startsWith(path)) {
            urls.add(toUrl(path));
          }
        }

      } else {
        urls.add(toUrl(path));
      }

      if (debug) {
        System.out.print("JarUrlFinder: " + path + " found in " + file);
        System.out.println(urls.stream().map(u -> u.getPath()).reduce("", (l, r) -> l + "\n" + r));
      }

      // }
      return urls;
    }
  }

  // jar:file://aaa/bbb/ccc/ddd
  protected URL toUrl(final String path) throws MalformedURLException {
    final String canonicalEntryPath = (path.startsWith("/") ? path : ("/" + path));
    final String fullPath = "jar:" + file.toURI().toURL() + "!" + canonicalEntryPath;
    return new URL(fullPath);
  }

  @Override
  public String toString() {
    return "jar[" + file.getAbsolutePath() + "]";
  }
}
