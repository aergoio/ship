package ship.command;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.res.Project;
import ship.util.FileWriter;

public class WriteProjectTargetTest extends AbstractTestCase {

  @Test
  @PrepareForTest(FileWriter.class)
  public void testExecute() throws Exception {
    // Given
    final byte[] content = randomUUID().toString().getBytes();
    final OutputStream out = mock(OutputStream.class);
    mockStatic(Files.class);
    when(Files.newOutputStream(any())).thenReturn(out);

    final ProjectFile projectFile = new ProjectFile();
    projectFile.setTarget(randomUUID().toString());
    final Project project = new Project(randomUUID().toString(), projectFile);
    final WriteProjectTarget writeProjectTarget = new WriteProjectTarget();
    writeProjectTarget.setProject(project);
    writeProjectTarget.setContents(() -> new ByteArrayInputStream(content));

    // When
    writeProjectTarget.execute();

    // Then
    verify(out).write(any(), anyInt(), anyInt());
  }
}