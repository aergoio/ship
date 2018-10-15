/*
 * @copyright defined in LICENSE.txt
 */

package ship.test;

import hera.api.encode.Base58WithCheckSum;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LuaBinary {
  @Getter
  protected final Supplier<Base58WithCheckSum> inputSupplier;

  public Base58WithCheckSum getPayload() {
    return inputSupplier.get();
  }

}
