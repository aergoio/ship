/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static hera.util.FilepathUtils.append;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

import hera.util.Adaptor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import ship.build.res.Project;

@RequiredArgsConstructor
public class Resource implements Adaptor {

  protected final transient Logger logger = getLogger(getClass());

  @Getter
  protected final Project project;

  @Getter
  protected final String location;

  public List<Resource> getDependencies(final ResourceManager resourceManager) throws Exception {
    return EMPTY_LIST;
  }

  public Path getPath() {
    final String path = append(project.getLocation(), location);
    return Paths.get(path);
  }

  @Override
  public <T> T adapt(final Class<T> adaptor) {
    if (adaptor.isInstance(this)) {
      return (T) this;
    }
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + location + "(" + project.getLocation() + ")";
  }
}
