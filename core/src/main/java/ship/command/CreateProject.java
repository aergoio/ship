/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.FilepathUtils.getFilename;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ship.util.Messages.bind;

import com.beust.jcommander.Parameter;
import hera.util.IoUtils;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ship.Command;
import ship.ProjectFile;
import ship.exception.CommandException;
import ship.exception.DirectoryNotEmptyException;
import ship.util.Messages;

public class CreateProject extends AbstractCommand implements Command {

  protected static final String NL_0 = CreateProject.class.getName() + ".0";
  protected static final String NL_1 = CreateProject.class.getName() + ".1";
  protected static final String NL_2 = CreateProject.class.getName() + ".2";

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

  /**
   * Prepare directories in project file(aergo.json).
   *
   * @param projectFile project configuration contents
   */
  protected void prepare(final ProjectFile projectFile) {
    final List<Supplier<String>> fileSupplier = asList(
        projectFile::getSource,
        projectFile::getTarget);

    fileSupplier.stream()
        .map(Supplier::get)
        .allMatch(path -> (null == path) ? true : of(path)
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::getParent)
            .filter(p -> !Files.exists(p))
            .map(Path::toFile)
            .map(File::mkdirs)
            .orElseThrow(() -> new CommandException(Messages.bind(NL_2, path))));
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
    prepare(newProjectFile);
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