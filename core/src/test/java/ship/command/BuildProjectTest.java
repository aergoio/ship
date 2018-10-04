package ship.command;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static ship.command.BuildProject.COMMAND_MODE;
import static ship.command.BuildProject.CONSOLE_MODE;
import static ship.command.BuildProject.WEB_MODE;

import java.io.IOException;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.Builder;
import ship.ProjectFile;
import ship.build.web.model.BuildDetails;
import ship.command.BuildProject.Options;

public class BuildProjectTest extends AbstractTestCase {

  @Test
  public void testGetOptions() {
    final BuildProject commandModeBuildProject = new BuildProject();
    final Options commandModeOptions = commandModeBuildProject.getOptions();
    assertEquals(COMMAND_MODE, commandModeOptions.getMode());

    final BuildProject consoleModeBuildProject = new BuildProject();
    consoleModeBuildProject.setArguments(asList("--watch"));
    final Options consoleModeOptions = consoleModeBuildProject.getOptions();
    assertEquals(CONSOLE_MODE, consoleModeOptions.getMode());

    final BuildProject webModeBuildProject = new BuildProject();
    webModeBuildProject.setArguments(asList("--port", "8080"));
    final Options webModeOptions = webModeBuildProject.getOptions();
    assertEquals(WEB_MODE, webModeOptions.getMode());

    final BuildProject webModeBuildProject2 = new BuildProject();
    webModeBuildProject2.setArguments(asList("--watch", "--port", "8080"));
    final Options webModeOptions2 = webModeBuildProject2.getOptions();
    assertEquals(WEB_MODE, webModeOptions2.getMode());
  }

  @Test
  @PrepareForTest(BuildProject.class)
  public void testExecute() throws Exception {
    final Builder builder = mock(Builder.class);
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setResult(randomUUID().toString());
    final WriteProjectTarget targetWriter = mock(WriteProjectTarget.class);

    whenNew(Builder.class).withAnyArguments().thenReturn(builder);
    when(builder.build(anyString())).thenReturn(buildDetails);

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
    final BuildDetails lastBuildDetails = buildProject.getLastBuildResult();
    logger.debug("Error: {}", lastBuildDetails.getError());

    verify(targetWriter).execute();
  }

}