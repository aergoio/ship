/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import ship.ProjectFile;

public class UpdatePackage extends AbstractCommand {
  @Override
  public void execute() throws Exception {
    final ProjectFile project = readProject();
  }
}
