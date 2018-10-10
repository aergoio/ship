package ship;

import static hera.util.IoUtils.from;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class FileContentTest extends AbstractTestCase {
  @Test
  public void testOpen() throws IOException {
    final byte[] bytes = randomUUID().toString().getBytes();
    final FileContent fileContent = new FileContent(
        randomUUID().toString(),
        () -> new ByteArrayInputStream(bytes));
    try (InputStream in = fileContent.open()) {
      assertArrayEquals(bytes, from(in));
    }
  }
  @Test(expected = IOException.class)
  public void shouldThrowIOException() throws IOException {
    final FileContent fileContent = new FileContent(randomUUID().toString(), () -> {
      throw new IOException();
    });
    try(InputStream in = fileContent.open()) {
      fail();
    }
  }

  @Test(expected = IOException.class)
  public void shouldThrowIOException2() throws IOException {
    final FileContent fileContent = new FileContent(randomUUID().toString(), () -> {
      throw new RuntimeException();
    });
    try(InputStream in = fileContent.open()) {
      fail();
    }
  }
}