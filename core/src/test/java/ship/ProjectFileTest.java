package ship;

import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

public class ProjectFileTest extends AbstractTestCase {

  @Test
  @PrepareForTest(ProjectFile.class)
  public void testFrom() throws IOException {
    Path path = mock(Path.class);
    mockStatic(Files.class);
    when(Files.newInputStream(path)).thenReturn(openWithExtensionAs("json"));
    ProjectFile file = ProjectFile.from(path);
    assertNotNull(file);
  }

  @Test
  public void testToJson() throws JsonProcessingException {
    final ProjectFile projectFile = new ProjectFile();
    assertNotNull(projectFile.toJson());
  }

}