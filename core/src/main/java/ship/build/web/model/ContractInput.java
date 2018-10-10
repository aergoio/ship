/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.model;

import lombok.Getter;
import lombok.Setter;

public class ContractInput {

  @Getter
  @Setter
  protected String name;

  @Getter
  @Setter
  protected String[] arguments = new String[0];
}
