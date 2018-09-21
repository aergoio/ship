/*
 * @copyright defined in LICENSE.txt
 */

package ship.exception;

public class NoBuildTargetException extends BuildException {
  public NoBuildTargetException() {
    super("No build target");
  }
}
