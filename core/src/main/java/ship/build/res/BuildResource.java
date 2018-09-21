/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import java.util.ArrayList;
import java.util.List;
import ship.ProjectFile;
import ship.build.Resource;
import ship.build.ResourceManager;

public class BuildResource extends Resource {
  public BuildResource(final Project project, final String path) {
    super(project, path);
  }

  @Override
  public List<Resource> getDependencies(final ResourceManager resourceManager) throws Exception {
    final ProjectFile projectFile = project.getProjectFile();

    final ArrayList<Resource> dependencies = new ArrayList<>();
    dependencies.add(resourceManager.getResource(projectFile.getSource()));

    return dependencies;
  }
}
