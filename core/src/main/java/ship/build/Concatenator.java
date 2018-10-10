/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static hera.util.FilepathUtils.getCanonicalForm;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import ship.build.res.Source;
import ship.build.web.model.BuildDependency;
import ship.build.web.model.BuildDetails;
import ship.exception.BuildException;

@RequiredArgsConstructor
public class Concatenator {

  protected final transient Logger logger = getLogger(getClass());

  protected final CallStack callStack = new CallStack();

  @Getter
  protected final ResourceManager resourceManager;

  @Getter
  protected final Set<Resource> visitedResources;

  public Concatenator(final ResourceManager resourceManager) {
    this(resourceManager, new LinkedHashSet<>());
  }

  @Getter
  @Setter
  protected String delimiter = "\n";

  protected String visit(final Source source) {
    if (visitedResources.contains(source)) {
      return null;
    }
    try {
      return source.getBody().get();
    } catch (final BuildException e) {
      throw e;
    } catch (final FileNotFoundException | NoSuchFileException ex) {
      throw new BuildException("<green>"
          + getCanonicalForm(source.getPath().toFile().getAbsolutePath()) + "</green> not found");
    } catch (final Throwable e) {
      throw new BuildException(e);
    }
  }

  /**
   * Visit resource for concatenation.
   *
   * @param resource resource to visit
   *
   * @return concatenated result
   */
  public BuildDetails visit(final Resource resource) {
    final BuildDependency dependencyRoot = new BuildDependency(null);
    dependencyRoot.setName(resource.getLocation());
    final String contents = visit(resource, dependencyRoot);

    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setResult(contents);
    buildDetails.setDependencies(dependencyRoot);
    return buildDetails;
  }

  /**
   * Visit resource with dependency.
   *
   * @param resource resource to concatenate
   * @param resourceDependency object to record dependencies
   *
   * @return concatenated bytes
   */
  public String visit(final Resource resource, final BuildDependency resourceDependency) {
    logger.trace("Resource: {}", resource);
    if (visitedResources.contains(resource)) {
      return null;
    }

    final StringJoiner contentWriter = new StringJoiner("\n");
    try {
      callStack.enter(resource);
      final Concatenator next = resource.adapt(ResourceManager.class)
          .map(newResourceManager -> new Concatenator(newResourceManager, visitedResources))
          .orElse(this);
      logger.debug("Resource manager changed: {} -> {}", this, next);
      visitDependencies(next, resource).forEach(childDependency -> {
        final Resource parent = childDependency.getParent();
        ofNullable(next.visit(parent, childDependency)).ifPresent(contentWriter::add);
        resourceDependency.add(childDependency);
      });

      resource.adapt(Source.class).map(next::visit).ifPresent(contentWriter::add);
      callStack.exit(resource);
      visitedResources.add(resource);
      return (0 < contentWriter.length()) ? contentWriter.toString() : null;
    } catch (final BuildException e) {
      throw e;
    } catch (final Throwable ex) {
      throw new BuildException(ex);
    }
  }

  protected Collection<BuildDependency> visitDependencies(final Concatenator next,
      final Resource resource) throws Exception {
    final ResourceManager nextResourceManager = next.getResourceManager();
    final List<Resource> dependencies = resource.getDependencies(nextResourceManager);
    logger.debug("Dependencies of {}: {}", resource, dependencies);
    final List<BuildDependency> resourceDependency = new ArrayList<>();
    for (final Resource dependency : dependencies) {
      final BuildDependency childDependency = new BuildDependency(dependency);
      childDependency.setName(dependency.getLocation());
      resourceDependency.add(childDependency);
    }
    return resourceDependency;
  }
}
