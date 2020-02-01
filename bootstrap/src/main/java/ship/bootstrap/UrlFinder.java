/*
 * @copyright defined in LICENSE.txt
 */

package ship.bootstrap;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface UrlFinder {

  /**
   * Find any url by {@code name}.
   *
   * @param name an name of url
   * @return an url. null if not exists
   *
   * @throws IOException if doesn't access from store
   */
  default URL find(String name) throws IOException {
    return findUrls(name).stream().findFirst().orElse(null);
  }

  /**
   * Find urls by {@code name}.
   *
   * @param name an name of url
   * @return urls
   *
   * @throws IOException if doesn't access from store
   */
  List<URL> findUrls(String name) throws IOException;

}
