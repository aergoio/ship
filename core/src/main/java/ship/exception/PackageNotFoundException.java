/*
 * @copyright defined in LICENSE.txt
 */

package ship.exception;

import ship.util.Messages;

public class PackageNotFoundException extends BuildException {

  protected static final String NL_0 = PackageNotFoundException.class.getName() + ".0";

  public PackageNotFoundException(final String packageName, final String location,
      final Throwable cause) {
    super(Messages.bind(NL_0, packageName, location), cause);
  }

}
