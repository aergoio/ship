package ship.exception;

import static ship.util.Messages.bind;

import java.nio.file.Path;

public class DirectoryNotEmptyException extends CommandException {
  public DirectoryNotEmptyException(final Path path) {
    super(bind(DirectoryNotEmptyException.class.getName(), path));
  }

}
