/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static hera.util.FilepathUtils.append;
import static hera.util.StringUtils.nvl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import ship.ProjectFile;
import ship.ShipConstants;
import ship.build.res.Project;
import ship.exception.PackageNotFoundException;

@RequiredArgsConstructor
public class PackageManager {

  protected final String repositoryLocation;

  public PackageManager() {
    this(append(nvl(System.getProperty("user.home"), System.getenv("HOME")),
        ShipConstants.MODULES_BASE));
  }

  /**
   * Find {@link ResourceManager} with package name.
   *
   * @param packageName package's name
   *
   * @return resource manager
   */
  public ResourceManager find(final String packageName) {
    try {
      final String packageLocation = append(repositoryLocation, packageName);
      final String projectFileLocation = append(packageLocation, ShipConstants.PROJECT_FILENAME);
      final Path projectFilePath = Paths.get(projectFileLocation);
      final ProjectFile projectFile = ProjectFile.from(projectFilePath);
      return new ResourceManager(new Project(packageLocation, projectFile));
    } catch (final IOException ex) {
      throw new PackageNotFoundException(ex);
    }
  }
}
