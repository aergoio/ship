/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.mockito.internal.verification.AtMost;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;

public class WriteProjectFileTest extends AbstractTestCase {

  @Test
  @PrepareForTest(WriteProjectFile.class)
  public void testExecute() throws Exception {
    // Given
    mockStatic(Files.class);
    final BufferedWriter bufferedWriter = mock(BufferedWriter.class);
    when(Files.newBufferedWriter(any(Path.class))).thenReturn(bufferedWriter);

    // When
    final WriteProjectFile command = new WriteProjectFile();
    command.setArguments(singletonList(randomUUID().toString()));
    command.execute();

    // Then
    verify(bufferedWriter).write(any(char[].class), anyInt(), anyInt());
  }

  @Test
  @PrepareForTest(WriteProjectFile.class)
  public void shouldNotCreateFile() throws Exception {
    // Given
    mockStatic(Files.class);
    when(Files.exists(any())).thenReturn(true);
    final BufferedWriter bufferedWriter = mock(BufferedWriter.class);
    when(Files.newBufferedWriter(any(Path.class))).thenReturn(bufferedWriter);

    // When
    final WriteProjectFile command = new WriteProjectFile();
    command.setArguments(singletonList(randomUUID().toString()));
    command.execute();

    // Then
    verify(bufferedWriter, new AtMost(0)).write(any(char[].class), anyInt(), anyInt());
  }
}