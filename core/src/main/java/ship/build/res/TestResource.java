/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import java.util.ArrayList;
import java.util.List;
import ship.ProjectFile;
import ship.build.Resource;
import ship.build.ResourceManager;

public class TestResource extends Source {
  public TestResource(final Project project, final String path) {
    super(project, path);
  }

  @Override
  public List<Resource> getDependencies(final ResourceManager resourceManager) throws Exception {
    final ProjectFile projectFile = project.getProjectFile();

    final ArrayList<Resource> dependencies = new ArrayList<>();
    dependencies.addAll(super.getDependencies(resourceManager));
    dependencies.add(resourceManager.getResource(projectFile.getSource()));
    return dependencies;
  }
}
