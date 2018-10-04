package ship.command;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.Builder;
import ship.ProjectFile;
import ship.build.web.model.BuildDetails;

public class BuildProjectCommandModeTest extends AbstractTestCase {

  @Test
  @PrepareForTest(BuildProjectCommandMode.class)
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
    final BuildProjectCommandMode buildProject = new BuildProjectCommandMode() {
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