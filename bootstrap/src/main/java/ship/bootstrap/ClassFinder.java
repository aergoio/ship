/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.IOException;
import java.net.URL;

public interface ClassFinder {

  /**
   * Find class byte code in path.
   *
   * @param path path to indicate a location
   *
   * @return class byte code
   *
   * @throws IOException if doesn't access from store
   */
  URL find(final String path) throws IOException;
}
