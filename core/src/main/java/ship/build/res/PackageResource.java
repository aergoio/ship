/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import static hera.util.ValidationUtils.assertNotNull;
import static java.util.Optional.of;

import java.util.Optional;
import ship.build.ResourceManager;

public class PackageResource extends BuildResource {

  protected final ResourceManager resourceManager;

  public PackageResource(final ResourceManager resourceManager) {
    super(resourceManager.getProject(), resourceManager.getProject().getLocation());
    assertNotNull(this.resourceManager = resourceManager);
  }

  @Override
  public <T> Optional<T> adapt(Class<T> adaptor) {
    if (adaptor.isInstance(resourceManager)) {
      return (Optional<T>) of(resourceManager);
    }
    return super.adapt(adaptor);
  }
}
