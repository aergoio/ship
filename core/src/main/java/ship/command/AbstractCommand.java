/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.FilepathUtils.getCanonicalForm;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import ship.ApmConstants;
import ship.Command;
import ship.ProjectFile;
import ship.util.DummyMessagePrinter;
import ship.util.MessagePrinter;

public abstract class AbstractCommand implements Command {

  protected final transient Logger logger = getLogger(getClass());

  @Getter
  @Setter
  protected List<String> arguments = emptyList();

  @Getter
  @Setter
  protected MessagePrinter printer = DummyMessagePrinter.getInstance();

  public String getArgument(final int index) {
    return arguments.get(index);
  }

  /**
   * Get {@code index} th argument.
   *
   * @param index argument index
   *
   * @return argument if exists
   */
  public Optional<String> getOptionalArgument(int index) {
    if (index < 0) {
      return empty();
    }
    if (index < arguments.size()) {
      return ofNullable(arguments.get(index));
    } else {
      return empty();
    }
  }

  public Path getProjectHome() {
    return Paths.get(".");
  }

  public Path getProjectFile() {
    return Paths.get(getProjectHome().toString(), ApmConstants.PROJECT_FILENAME);
  }

  public String getProjectHomePath() {
    return getCanonicalForm(getProjectHome().toFile().getAbsolutePath());
  }

  public String getProjectFilePath() {
    return getCanonicalForm(getProjectFile().toFile().getAbsolutePath());
  }

  /**
   * Read project file.
   *
   * @return project
   *
   * @throws IOException On failure to read project file
   */
  public ProjectFile readProject() throws IOException {
    return ProjectFile.from(getProjectFile());
  }
}
