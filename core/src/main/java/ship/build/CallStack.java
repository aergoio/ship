/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Stack;
import java.util.StringJoiner;
import org.slf4j.Logger;
import ship.exception.CyclicDependencyException;

public class CallStack {
  protected final transient Logger logger = getLogger(getClass());

  protected final Stack<Resource> stack = new Stack<>();

  /**
   * Notify to enter call.
   *
   * @param resource resource to call
   */
  public void enter(final Resource resource) {
    if (stack.contains(resource)) {
      throw new CyclicDependencyException(resource.toString() + " duplicated in " + this);
    }
    stack.push(resource);
  }

  /**
   * Notify to exit call.
   *
   * @param resource resource to call
   */
  public void exit(final Resource resource) {
    stack.remove(resource);
  }

  @Override
  public String toString() {
    final StringJoiner joiner = new StringJoiner("->");
    stack.stream().map(Object::toString).forEach(joiner::add);
    return joiner.toString();
  }
}
