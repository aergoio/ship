/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;
import ship.build.Concatenator;
import ship.build.Resource;
import ship.build.ResourceManager;
import ship.build.res.BuildResource;
import ship.build.res.TestResource;
import ship.build.web.model.BuildDetails;

public class Builder {

  protected final transient Logger logger = getLogger(getClass());

  @Getter
  protected final ResourceManager resourceManager;

  /**
   * Constructor with project file(aergo.json).
   *
   * @param resourceManager manager for resource
   */
  public Builder(final ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  /**
   * Build {@link BuildResource} or {@link TestResource}.
   *
   * @param resourcePath base to resource
   *
   * @return Build result fileset
   */
  public BuildDetails build(final String resourcePath) {
    final Resource resource = resourceManager.getResource(resourcePath);
    logger.trace("{}: {}", resourcePath, resource);
    final Concatenator concatenator = new Concatenator(resourceManager);
    final Optional<BuildResource> buildResource = ofNullable(resource.adapt(BuildResource.class));
    final Optional<TestResource> testResource = ofNullable(resource.adapt(TestResource.class));
    if (buildResource.isPresent()) {
      logger.trace("Build to target");
      return concatenator.visit(buildResource.get());
    } else if (testResource.isPresent()) {
      logger.trace("Build to test");
      return concatenator.visit(testResource.get());
    }

    return new BuildDetails();
  }
}
