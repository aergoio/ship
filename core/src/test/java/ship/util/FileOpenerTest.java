package ship.util;

import static hera.util.IoUtils.from;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;

public class FileOpenerTest extends AbstractTestCase {
  @Test
  @PrepareForTest(FileOpener.class)
  public void testOpen() throws IOException {
    final byte[] content = randomUUID().toString().getBytes();
    mockStatic(Files.class);
    ByteArrayInputStream inMock = new ByteArrayInputStream(content);

    Mockito.when(Files.newInputStream(any(Path.class))).thenReturn(inMock);

    final Path path = mock(Path.class);
    final FileOpener fileOpener = new FileOpener(path);
    try (final InputStream in = fileOpener.get()) {
      assertArrayEquals(content, from(in));
    }
  }
}