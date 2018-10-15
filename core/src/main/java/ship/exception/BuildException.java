/*
 * @copyright defined in LICENSE.txt
 */

package ship.exception;

public class BuildException extends CommandException {
  public BuildException(final String message) {
    super(message);
  }

  public BuildException(final Throwable cause) {
    super(cause);
  }

  public BuildException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
