/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ship.ship.util.IoUtils;

@RequiredArgsConstructor
public class Loader extends ClassLoader implements Debuggable {
  protected final UrlFinder finder;

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
        System.out.printf("findClass(%s): UNEXPECTED exception finding url", name);
        ex.printStackTrace();
      }
    }

    if (null == url) {
      if (debug) {
        System.out.printf("findClass(%s): url is null%n", name);
      }
      return super.findClass(name);
    }

    try {
      byte[] input = null;
      try (final InputStream in = url.openStream()) {
        input = IoUtils.readFully(in, 1000);
      }
      final Class<?> clazz = defineClass(name, input, 0, input.length);
      // define package
      final int i = name.lastIndexOf('.');
      final String packageName = name.substring(0, i);
      if (null == getPackage(packageName)) {
        definePackage(packageName, null, null, null, null, null, null, null);
      }
      return clazz;
    } catch (Throwable ex) {
      ex.printStackTrace();
      if (debug) {
        System.out.printf("findClass(%s): UNEXPECTED Exception%n", name);
        ex.printStackTrace();
      }
      return super.findClass(name);
    }
  }

  @Override
  protected URL findResource(final String name) {
    if (debug) {
      System.out.printf("findResource(%s)%n", name);
    }
    try {
      final URL resource = finder.find(name);
      if (null == resource) {
        return super.findResource(name);
      }
      return resource;
    } catch (final Throwable ex) {
      return super.findResource(name);
    }
  }

  @Override
  protected Enumeration<URL> findResources(final String name) throws IOException {
    if (debug) {
      System.out.printf("findResources(%s)%n", name);
    }
    final Enumeration<URL> fromParent = super.findResources(name);
    final Collection<URL> parentUrls = new ArrayList<>();
    while (fromParent.hasMoreElements()) {
      parentUrls.add(fromParent.nextElement());
    }
    final List<URL> urls = Stream.concat(finder.findUrls(name).stream(), parentUrls.stream())
        .collect(toList());
    return new Enumeration<URL>() {
      protected Iterator<URL> it = urls.iterator();

      @Override
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      @Override
      public URL nextElement() {
        return it.next();
      }
    };
  }
}
