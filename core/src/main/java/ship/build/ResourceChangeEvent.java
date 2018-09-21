/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ResourceChangeEvent {
  @Getter
  protected final Resource resource;
}
