package ship.util;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashMap;
import org.junit.Test;
import ship.AbstractTestCase;

public class AnsiMessagePrinterTest extends AbstractTestCase {

  @Test
  public void testFormat() {
    final String resetCode = randomUUID().toString();
    final String blue = randomUUID().toString();
    final HashMap<String, String> colors = new HashMap<>();
    colors.put("blue", blue);
    colors.put("red", blue);
    final AnsiMessagePrinter printer = new AnsiMessagePrinter(mock(PrintStream.class));
    printer.setResetCode(resetCode);
    printer.setColors(colors);
    final String encoded = printer.format("<blue>hello, world</blue>");
    logger.debug("Encoded: {}", encoded);
    assertTrue(encoded.startsWith(blue));
    assertTrue(encoded.contains("hello"));
    assertTrue(encoded.contains("world"));
    assertTrue(encoded.endsWith(resetCode));
  }

  @Test
  public void shouldNotThrowException() {
    final String resetCode = randomUUID().toString();
    final String blue = randomUUID().toString();
    final HashMap<String, String> colors = new HashMap<>();
    colors.put("blue", blue);
    colors.put("red", blue);
    final AnsiMessagePrinter printer = new AnsiMessagePrinter(mock(PrintStream.class));
    printer.setResetCode(resetCode);
    printer.setColors(colors);

    printer.format("<red>No command!!</red>");
    printer.println(" <red>x</red> {0} - <red>{1}</red>", "src/test/lua/test-asset.lua", "assertion failed!");
  }

}