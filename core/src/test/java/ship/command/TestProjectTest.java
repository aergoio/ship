package ship.command;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
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
    buildDetails.setResult("");
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