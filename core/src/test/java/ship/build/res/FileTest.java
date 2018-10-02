package ship.build.res;

import static java.util.UUID.randomUUID;

import java.io.IOException;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;

public class FileTest extends AbstractTestCase {

  @Test(expected = IOException.class)
  public void shouldThrowIOException() throws IOException {
    final Project project = new Project(randomUUID().toString(), new ProjectFile());
    final File file = new File(project, randomUUID().toString());
    file.open();
  }
}