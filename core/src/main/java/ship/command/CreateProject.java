/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.FilepathUtils.getFilename;
import static java.util.Collections.singletonList;
import static ship.util.Messages.bind;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import hera.util.IoUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ship.Command;
import ship.ProjectFile;
import ship.ShipConstants;
import ship.exception.DirectoryNotEmptyException;

public class CreateProject extends AbstractCommand implements Command {

  protected static final String NL_0 = CreateProject.class.getName() + ".0";
  protected static final String NL_1 = CreateProject.class.getName() + ".1";

  @ToString
  class Options {
    @Parameter(names = {"-f", "--force"},
        description = "Force to set aergo.json up though it exists.")
    @Getter
    @Setter
    protected boolean force = false;
  }

  @Override
  public void execute() throws Exception {
    logger.trace("Starting {}...", this);
    logger.debug("Arguments: {}", arguments);

    final Options options = new Options();
    JCommander.newBuilder().addObject(options).build().parse(arguments.toArray(new String[0]));
    logger.debug("Options: {}", options);

    final Path projectPath = Paths.get(".").toFile().getAbsoluteFile().getCanonicalFile().toPath();
    logger.trace("Project location: {}", projectPath);

    final Path projectFilePath = Paths.get(projectPath.toString(), ShipConstants.PROJECT_FILENAME);

    final WriteProjectFile writeProjectFile = new WriteProjectFile();
    final String projectDirectoryName = getFilename(projectPath.toFile().getCanonicalPath());
    if (!options.isForce() && 0 < Files.list(projectPath).count()) {
      logger.trace("Force: {}", options.isForce());
      throw new DirectoryNotEmptyException(projectPath);
    }
    final String projectName = System.getProperty("user.name") + "/" + projectDirectoryName;
    logger.debug("Project name: {}", projectName);
    final ProjectFile projectFile = writeProjectFile.getProject();
    projectFile.setName(projectName);
    projectFile.setSource("src/main/lua/main.lua");
    projectFile.setTarget("app.lua");
    projectFile.setEndpoint(null);
    logger.trace("Project file: {}", projectFile);
    writeProjectFile.setArguments(singletonList(projectFilePath.toAbsolutePath().toString()));
    writeProjectFile.execute();

    printer.println(bind(NL_0, projectPath));
    try (
        final InputStream in = Files.newInputStream(projectFilePath);
        final Reader reader = new InputStreamReader(in)) {
      printer.println(bind(NL_1, projectFilePath));
      printer.println(IoUtils.from(reader));
    }
  }
}