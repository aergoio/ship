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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ship.Command;
import ship.ProjectFile;
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

  protected ProjectFile newProjectFile(final String projectName) {
    logger.debug("Project name: {}", projectName);
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setName(projectName);
    projectFile.setSource("src/main/lua/main.lua");
    projectFile.setTarget("app.lua");
    projectFile.setEndpoint(null);
    logger.trace("Project file: {}", projectFile);
    return projectFile;
  }

  @Override
  public void execute() throws Exception {
    logger.debug("Starting {} with {}...", this, arguments);

    final Options options = parse(new Options());
    final Path projectPath = getProjectHome();
    final String projectPathStr = getProjectHomePath();
    final Path projectFilePath = getProjectFile();
    final String projectFilePathStr = getProjectFilePath();

    final WriteProjectFile writeProjectFile = new WriteProjectFile();
    final String projectDirectoryName = getFilename(projectPathStr);
    if (!options.isForce() && 0 < Files.list(projectPath).count()) {
      logger.trace("Force: {}", options.isForce());
      throw new DirectoryNotEmptyException(projectPath);
    }
    final String projectName = System.getProperty("user.name") + "/" + projectDirectoryName;
    final ProjectFile newProjectFile = newProjectFile(projectName);
    writeProjectFile.setProject(newProjectFile);
    writeProjectFile.setArguments(singletonList(projectFilePathStr));
    writeProjectFile.execute();

    printer.println(bind(NL_0, projectPathStr));
    try (
        final InputStream in = Files.newInputStream(projectFilePath);
        final Reader reader = new InputStreamReader(in)) {
      printer.println(bind(NL_1, projectFilePathStr));
      printer.println(IoUtils.from(reader));
    }
  }
}