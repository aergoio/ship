/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static hera.util.FilepathUtils.getCanonicalForm;
import static hera.util.ObjectUtils.equal;
import static hera.util.ObjectUtils.nvl;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableSet;
import static org.slf4j.LoggerFactory.getLogger;

import hera.server.ServerEvent;
import hera.server.ServerListener;
import hera.util.FilepathUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import ship.ProjectFile;
import ship.build.res.BuildResource;
import ship.build.res.PackageResource;
import ship.build.res.Project;
import ship.build.res.Source;
import ship.build.res.TestResource;
import ship.util.FileWatcher;

@RequiredArgsConstructor
public class ResourceManager implements ServerListener {
  protected static final Set<Integer> eventFilter =
      unmodifiableSet(new HashSet(asList(FileWatcher.ANY_CHANGED)));

  protected final transient Logger logger = getLogger(getClass());

  protected final Map<String, Resource> cache = new HashMap<>();

  @Getter
  protected final Project project;

  @Getter
  @Setter
  protected PackageManager packageManager = new PackageManager();

  protected final Set<ResourceChangeListener> resourceChangeListeners = new HashSet<>();

  public void addResourceChangeListener(final ResourceChangeListener listener) {
    resourceChangeListeners.add(listener);
  }

  public void removeResourceChangeListener(final ResourceChangeListener listener) {
    resourceChangeListeners.remove(listener);
  }

  protected void fireEvent(final ResourceChangeEvent event) {
    for (final ResourceChangeListener listener : resourceChangeListeners) {
      listener.handle(event);
    }
  }

  /**
   * Return resource for {@code base}.
   *
   * @param path resource base
   *
   * @return resource for base
   */
  public synchronized Resource getResource(final String path) {
    logger.trace("Path: {}", path);
    final ProjectFile projectFile = project.getProjectFile();
    final String canonicalPath = getCanonicalForm(path);
    logger.trace("Canonical base: {}", canonicalPath);
    final Resource cached = cache.get(canonicalPath);
    if (null != cached) {
      return cached;
    }
    final Resource created = create(canonicalPath);
    cache.put(canonicalPath, created);
    logger.debug("{} added", canonicalPath);
    return created;
  }

  protected boolean isTarget(final String canonicalPath) {
    final ProjectFile projectFile = project.getProjectFile();
    return equal(canonicalPath, getCanonicalForm(projectFile.getTarget()));
  }

  protected boolean isTest(final String canonicalPath) {
    final ProjectFile projectFile = project.getProjectFile();
    return nvl(projectFile.getTests(), Collections.<String>emptyList())
        .stream().map(FilepathUtils::getCanonicalForm).anyMatch(canonicalPath::equals);
  }

  protected Resource create(final String canonicalPath) {
    if (isTarget(canonicalPath)) {
      return new BuildResource(project, canonicalPath);
    } else {
      if (isTest(canonicalPath)) {
        return new TestResource(project, canonicalPath);
      } else {
        if (canonicalPath.endsWith(".lua")) {
          return new Source(project, canonicalPath);
        } else {
          return new Resource(project, canonicalPath);
        }
      }
    }

  }

  public Resource getPackage(final String packageName) {
    final ResourceManager newResourceManager = packageManager.find(packageName);
    return new PackageResource(newResourceManager);
  }

  @Override
  public void handle(final ServerEvent event) {
    if (!eventFilter.contains(event.getType())) {
      logger.trace("Unhandled event: {}", event);
      return;
    }

    final Collection<File> files = nvl((Collection<File>) event.getNewData(), EMPTY_LIST);
    final Optional<Resource> cachedResourceOpt = files.stream().map(file -> {
      final String path = getCanonicalForm(file.getAbsolutePath());
      final String projectPath =
          getCanonicalForm(project.getPath().toAbsolutePath().toString());
      final Path relativePath =
          Paths.get(projectPath).relativize(Paths.get(path));
      logger.trace("Project path: {}, Path: {}, Relative path: {}",
          projectPath, path, relativePath);
      final String relativePathStr = relativePath.toString();
      logger.debug("Relative path: {}", relativePath);

      final String canonicalPath = getCanonicalForm(relativePathStr);
      logger.trace("Project relative path: {}", canonicalPath);
      return cache.get(canonicalPath);
    }).filter(Objects::nonNull).findFirst();
    if (cachedResourceOpt.isPresent()) {
      final Resource cached = cachedResourceOpt.get();
      logger.info("{} changed: {}", cached, event.getType());
      fireEvent(new ResourceChangeEvent(cached));
    }
  }
}
