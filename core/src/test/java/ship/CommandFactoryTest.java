package ship;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import ship.util.DummyMessagePrinter;

@RequiredArgsConstructor
public class CommandFactoryTest extends AbstractTestCase {
  @Parameters
  public static Collection<String> data() {
    return Arrays.asList("init", "install", "build", "test", "publish");
  }

  @Test
  public void testCreate() {
    final CommandFactory commandFactory = new CommandFactory(DummyMessagePrinter.getInstance());
    for (final String command : data()) {
      assertTrue(commandFactory.create(new String[] { command }).isPresent());
    }
  }

}