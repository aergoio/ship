/*
 * @copyright defined in LICENSE.txt
 */

package ship.test;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import ship.AbstractTestCase;

public class LuaRunnerTest extends AbstractTestCase {
  @Test
  public void shouldNotReservePreviousScript() {
    final LuaRunner runner = new LuaRunner();
    final LuaSource luaSource = new LuaSource("a = 3\nb=3");
    runner.run(luaSource);
    assertNull(runner.run(new LuaSource("return b")).getResult());
  }
}