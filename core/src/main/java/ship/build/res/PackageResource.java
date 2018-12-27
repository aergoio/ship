/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import static hera.util.ValidationUtils.assertNotNull;

import ship.build.ResourceManager;

public class PackageResource extends BuildResource {

  protected final ResourceManager resourceManager;

  public PackageResource(final ResourceManager resourceManager) {
    super(resourceManager.getProject(), resourceManager.getProject().getLocation());
    assertNotNull(this.resourceManager = resourceManager);
  }

  @Override
  public <T> T adapt(Class<T> adaptor) {
    if (adaptor.isInstance(resourceManager)) {
      return (T) resourceManager;
    }
    return super.adapt(adaptor);
  }
}
