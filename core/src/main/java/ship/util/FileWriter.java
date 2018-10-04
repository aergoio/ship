package ship.util;

import hera.util.DangerousConsumer;
import hera.util.IoUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileWriter implements DangerousConsumer<InputStream> {

  protected final Path path;

  @Override
  public void accept(final InputStream inputStream) throws Exception {
    try (final OutputStream out = Files.newOutputStream(path)) {
      IoUtils.redirect(inputStream, out);
    }
  }
}
