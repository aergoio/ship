/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.model;

import hera.api.model.ContractInterface;
import lombok.Getter;
import lombok.Setter;

public class DeploymentResult {
  @Getter
  @Setter
  protected String buildUuid;

  @Getter
  @Setter
  protected String encodedContractTransactionHash;

  @Getter
  @Setter
  protected ContractInterface contractInterface;

  public String toString() {
    return "Transaction[" + getEncodedContractTransactionHash() + "]";
  }
}
