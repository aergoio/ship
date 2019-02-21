package ship.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.ResourceChangeEvent;
import ship.build.web.model.BuildDetails;
import ship.util.DangerousConsumer;
import ship.util.FileWatcher;

@PrepareForTest(BuildProjectConsoleMode.class)
public class BuildProjectConsoleModeTest extends AbstractTestCase {

  protected final ProjectFile projectFile = new ProjectFile();

  protected final BuildProjectConsoleMode buildProject = spy(new BuildProjectConsoleMode() {
    @Override
    public ProjectFile readProject() {
      return projectFile;
    }
  });

  @Mock
  protected DangerousConsumer<BuildDetails> listener;

  @Before
  public void setUp() throws IOException {
    buildProject.addListener(listener);
    buildProject.initialize();
  }

  @Test
  public void testExecute() throws Exception {
    // Given
    final FileWatcher fileWatcher = mock(FileWatcher.class);
    whenNew(FileWatcher.class).withAnyArguments().thenReturn(fileWatcher);

    // When
    buildProject.execute();
  }

  @Test
  public void testResourceChanged() throws Exception {
    // Given
    final ResourceChangeEvent event = new ResourceChangeEvent(null);

    // When
    buildProject.resourceChanged(event);

    // Then
    verify(listener).accept(any());
  }

}