package ship.command;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;

public class BuildProjectTest extends AbstractTestCase {
  @Test
  public void testExecute() throws Exception {
    final WriteProjectTarget targetWriter = mock(WriteProjectTarget.class);

    final ProjectFile projectFile = new ProjectFile();
    projectFile.setSource(randomUUID().toString());
    projectFile.setTarget(randomUUID().toString());
    final BuildProject buildProject = new BuildProject() {
      @Override
      public ProjectFile readProject() throws IOException {
        return projectFile;
      }
    };
    buildProject.setTargetWriter(targetWriter);
    buildProject.execute();

    verify(targetWriter).execute();
  }

}