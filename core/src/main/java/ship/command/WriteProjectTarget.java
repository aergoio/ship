package ship.command;

import hera.util.DangerousSupplier;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import ship.build.res.Project;
import ship.util.FileWriter;

public class WriteProjectTarget extends AbstractCommand {

  @Getter
  protected Project project;

  @Setter
  protected DangerousSupplier<InputStream> contents;

  public void setProject(final Project project) {
    this.project = project;
  }

  @Override
  public void execute() throws Exception {
    final String buildTarget = project.getProjectFile().getTarget();
    final Path buildTargetPath = project.getPath().resolve(buildTarget);

    final FileWriter fileWriter = new FileWriter(buildTargetPath);
    fileWriter.accept(contents.get());
  }
}
