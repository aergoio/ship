/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.ValidationUtils.assertTrue;
import static java.nio.file.Files.newBufferedWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import ship.ProjectFile;

public class WriteProjectFile extends AbstractCommand {

  @Getter
  @Setter
  protected ProjectFile project = new ProjectFile();

  @Override
  public void execute() throws Exception {
    assertTrue(1 == arguments.size());
    final Path projectFilePath = Paths.get(arguments.get(0));
    final ObjectMapper mapper = new ObjectMapper();
    if (Files.exists(projectFilePath)) {
      logger.warn("Project file already exists");
      return;
    }
    try (final BufferedWriter writer = newBufferedWriter(projectFilePath)) {
      mapper.writerWithDefaultPrettyPrinter().writeValue(writer, project);
    }
  }
}
