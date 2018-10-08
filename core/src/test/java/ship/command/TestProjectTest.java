package ship.command;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import hera.util.IoUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.Builder;
import ship.ProjectFile;
import ship.build.web.model.BuildDetails;

public class TestProjectTest extends AbstractTestCase {
  @Test
  public void testExecute() throws Exception {
    // Given
    final Builder builder = mock(Builder.class);
    final BuildDetails buildDetails = new BuildDetails();
    try(final InputStreamReader reader = new InputStreamReader(openWithExtensionAs("lua"))) {
      buildDetails.setResult(IoUtils.from(reader));
    }
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setTests(asList(randomUUID().toString()));
    final TestProject testProject = new TestProject() {
      @Override
      public ProjectFile readProject() throws IOException {
        return projectFile;
      }
    };
    testProject.setBuilderFactory(project -> builder);
    when(builder.build(anyString())).thenReturn(buildDetails);

    // When
    testProject.execute();

    // Then
  }

}