/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static com.google.common.io.MoreFiles.deleteRecursively;
import static com.google.common.io.RecursiveDeleteOption.ALLOW_INSECURE;
import static hera.util.FilepathUtils.append;
import static hera.util.ValidationUtils.assertNotNull;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static ship.util.Messages.bind;

import java.nio.file.Path;
import java.nio.file.Paths;
import ship.FileSet;
import ship.ProjectFile;

public class PublishPackage extends AbstractCommand {

  protected static final String NL_0 = PublishPackage.class.getName() + ".0";
  protected static final String NL_1 = PublishPackage.class.getName() + ".1";
  protected static final String NL_2 = PublishPackage.class.getName() + ".2";

  @Override
  public void execute() throws Exception {
    logger.trace("Starting {}...", this);

    final ProjectFile rootProject = readProject();
    final String buildTarget = rootProject.getTarget();
    assertNotNull(buildTarget, bind(NL_0));

    if (!exists(Paths.get(buildTarget))) {
      new BuildProject().execute();
    }

    final String publishRepository = append(System.getProperty("user.home"), ".aergo_modules");
    final Path publishPath = Paths.get(append(publishRepository, rootProject.getName()));
    if (exists(publishPath)) {
      deleteRecursively(publishPath, ALLOW_INSECURE);
    }
    createDirectories(publishPath);
    FileSet.from(Paths.get(".")).copyTo(publishPath);
    printer.println(NL_1, rootProject.getName());
    printer.println(NL_2, publishPath);
  }
}
