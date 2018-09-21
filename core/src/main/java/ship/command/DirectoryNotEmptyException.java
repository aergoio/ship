package ship.command;

import java.nio.file.Path;
import ship.exception.CommandException;

public class DirectoryNotEmptyException extends CommandException {
  public DirectoryNotEmptyException(Path path) {
    super("<yellow>" + path.toString() + "</yellow> is not empty."
        + " You can reset project with <blue>-f</blue> but It make aergo.json overwritten!!");
  }

}
