/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class CompositeUrlFinder implements UrlFinder, Debuggable {

  protected final List<UrlFinder> finders;

  @Getter
  @Setter
  protected boolean debug = false;

  @Override
  public List<URL> findUrls(final String name) throws IOException {
    return finders.stream()
        .map(finder -> findInternal(finder, name))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  protected URL findInternal(final UrlFinder finder, final String path) {
    try {
      return finder.find(path);
    } catch (final Exception e) {
      System.out.println("finder: " + finder);
      if (debug) {
        System.out.println("WARN: Fail to read " + path + " in " + finder);
      }
      return null;
    }
  }
}
