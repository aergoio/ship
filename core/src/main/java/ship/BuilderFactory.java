/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import ship.build.res.Project;

public interface BuilderFactory {

  /**
   * Create builder for {@code project}.
   *
   * @param project project of builder
   *
   * @return builder to create
   */
  Builder create(final Project project);
}
