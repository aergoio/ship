/*
 * @copyright defined in LICENSE.txt
 */

package ship.test;

import static hera.util.IoUtils.from;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.junit.Test;
import ship.AbstractTestCase;

public class LuaRunnerTest extends AbstractTestCase {
  @Test
  public void shouldNotReservePreviousScript() throws IOException {
    final LuaRunner runner = new LuaRunner();
    try (final Reader reader = new InputStreamReader(openWithExtensionAs("lua"))) {
      final LuaSource luaSource = new LuaSource(from(reader));
      final TestResult result = runner.run(luaSource);
      assertNotNull(result.getError());
      assertNull(runner.run(new LuaSource("return b")).getResult());
    }
  }
}