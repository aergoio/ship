package ship.command;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.ProjectFile;

public class PublishPackageTest extends AbstractTestCase {

  @Test
  @PrepareForTest(PublishPackage.class)
  public void testExecute() throws Exception {
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setTarget(randomUUID().toString());
    final PublishPackage publishPackage = new PublishPackage() {
      @Override
      public ProjectFile readProject() throws IOException {
        return projectFile;
      }
    };
    final BuildProject buildProject = mock(BuildProject.class);
    whenNew(BuildProject.class).withAnyArguments().thenReturn(buildProject);
    mockStatic(Files.class);
    publishPackage.execute();

  }

}