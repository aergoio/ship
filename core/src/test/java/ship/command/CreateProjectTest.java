/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static java.util.Collections.EMPTY_LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.util.MessagePrinter;

public class CreateProjectTest extends AbstractTestCase {

  @Test
  @PrepareForTest({CreateProject.class, File.class})
  @SuppressWarnings("unchecked")
  public void testExecute() throws Exception {
    // Given
    final ProjectFile projectFile = new ProjectFile();
    final WriteProjectFile writeProjectFile = mock(WriteProjectFile.class);
    final CreateProject command = spy(new CreateProject());

    whenNew(ProjectFile.class).withAnyArguments().thenReturn(projectFile);
    whenNew(WriteProjectFile.class).withAnyArguments().thenReturn(writeProjectFile);
    mockStatic(Files.class);
    when(Files.list(any())).thenReturn(EMPTY_LIST.stream());
    when(Files.newInputStream(any()))
        .thenReturn(new ByteArrayInputStream("hello, world".getBytes()));
    doNothing().when(command).prepare(any());

    // When
    command.setPrinter(mock(MessagePrinter.class));
    command.execute();

    // Then
    verify(writeProjectFile).execute();
  }
}