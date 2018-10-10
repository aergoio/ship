package ship.build.res;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.Resource;
import ship.build.ResourceManager;

public class TestResourceTest extends AbstractTestCase {

  @Test
  public void testGetDependencies() throws Exception {
    // Given
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setSource(randomUUID().toString());
    projectFile.setTarget(randomUUID().toString());
    final Project project = new Project(randomUUID().toString(), projectFile);
    final ResourceManager resourceManager = new ResourceManager(project);
    final TestResource testResource = new TestResource(project, randomUUID().toString());

    // When
    final List<Resource> resources = testResource.getDependencies(resourceManager);

    // Then
    assertTrue(0 < resources.size());
  }

}