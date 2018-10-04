package ship.command;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static ship.command.BuildProject.COMMAND_MODE;
import static ship.command.BuildProject.CONSOLE_MODE;
import static ship.command.BuildProject.WEB_MODE;

import java.util.function.Function;
import org.junit.Test;
import ship.AbstractTestCase;
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
  public void testExecute() throws Exception {
    // Given
    final BuildProjectCommandMode buildProjectCommandMode = mock(BuildProjectCommandMode.class);
    final Function<Options, BuildProjectCommandMode> factory = options -> buildProjectCommandMode;
    final BuildProject buildProject = new BuildProject();
    buildProject.setCommandFactory(factory);

    // When
    buildProject.execute();

    // Then
    verify(buildProjectCommandMode).execute();
  }
}