/*
 * @copyright defined in LICENSE.txt
 */

package ship.exception;

public class CyclicDependencyException extends BuildException {

  public CyclicDependencyException(final String message) {
    super(message);
  }

}
