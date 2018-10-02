package ship.build.res;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.ResourceManager;

public class PackageResourceTest extends AbstractTestCase {

  @Test
  public void testAdapt() {
    final Project project = new Project(randomUUID().toString(), new ProjectFile());
    final ResourceManager resourceManager = new ResourceManager(project);
    final PackageResource packageResource = new PackageResource(resourceManager);
    assertTrue(packageResource.adapt(ResourceManager.class).isPresent());
  }

}