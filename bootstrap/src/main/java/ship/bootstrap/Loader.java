/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ship.ship.util.IoUtils;

@RequiredArgsConstructor
public class Loader extends ClassLoader implements Debuggable {
  protected final ClassFinder finder;

  @Getter
  @Setter
  protected boolean debug = false;

  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    final String path = name.replace('.', '/') + ".class";
    URL url = null;
    try {
      url = finder.find(path);
    } catch (Throwable ex) {
      if (debug) {
        System.out.println("UNEXPECTED Exception");
        ex.printStackTrace();
      }
    }
    if (null == url) {
      return super.findClass(name);
    } else {
      try {
        byte[] input = null;
        try (final InputStream in = url.openStream()) {
          input = IoUtils.readFully(in, 1000);
        }
        return defineClass(name, input, 0, input.length);
      } catch (Throwable ex) {
        if (debug) {
          System.out.println("UNEXPECTED Exception");
          ex.printStackTrace();
        }
        return super.findClass(name);
      }
    }
  }

  @Override
  protected URL findResource(String name) {
    if (debug) {
      System.out.println("Find " + name);
    }
    try {
      return finder.find(name);
    } catch (final Throwable ex) {
      return null;
    }
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    if (debug) {
      System.out.println("Find " + name);
    }
    return super.findResources(name);
  }
}
