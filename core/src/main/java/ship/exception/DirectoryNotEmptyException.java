package ship.exception;

import static ship.util.Messages.bind;

import java.nio.file.Path;

public class DirectoryNotEmptyException extends CommandException {
  protected static final String NL_0 = DirectoryNotEmptyException.class.getName() + ".0";
  public DirectoryNotEmptyException(final Path path) {
    super(bind(NL_0, path));
  }

}
