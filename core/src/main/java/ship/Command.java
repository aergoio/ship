/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import java.util.List;

public interface Command {
  void setArguments(List<String> arguments);

  void execute() throws Exception;
}
