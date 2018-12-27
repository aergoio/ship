/*
 * @copyright defined in LICENSE.txt
 */

package ship.test;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LuaBinary {
  @Getter
  protected final Supplier<String> inputSupplier;

  public String getPayload() {
    return inputSupplier.get();
  }

}
