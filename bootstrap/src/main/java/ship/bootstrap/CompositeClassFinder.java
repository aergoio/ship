/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class CompositeClassFinder implements ClassFinder, Debuggable {

  protected final List<ClassFinder> finders;

  @Getter @Setter
  protected boolean debug = false;

  @Override
  public URL find(final String path) {
    return finders.stream()
        .map(finder -> findInternal(finder, path))
        .filter(Objects::nonNull)
        .findAny().orElse(null);
  }

  protected URL findInternal(final ClassFinder finder, final String path) {
    try {
      return finder.find(path);
    } catch (final Exception e) {
      if (debug) {
        System.out.println("WARN: Fail to read " + path + " in " + finder);
      }
      return null;
    }
  }
}
