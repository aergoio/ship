package ship.command;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.util.FileWatcher;

public class BuildProjectWebModeTest extends AbstractTestCase {
  @Test
  @PrepareForTest(BuildProjectWebMode.class)
  public void testExecute() throws Exception {
    // Given
    final ProjectFile projectFile = new ProjectFile();

    final BuildProjectWebMode buildProject = new BuildProjectWebMode(0) {
      @Override
      public ProjectFile readProject() {
        return projectFile;
      }
    };

    final FileWatcher fileWatcher = mock(FileWatcher.class);
    whenNew(FileWatcher.class).withAnyArguments().thenReturn(fileWatcher);

    // When
    buildProject.execute();

  }

}