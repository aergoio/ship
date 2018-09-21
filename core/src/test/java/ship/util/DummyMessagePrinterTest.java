package ship.util;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;
import ship.AbstractTestCase;

public class DummyMessagePrinterTest extends AbstractTestCase {

  @Test
  public void testGetInstance() {
    assertNotNull(DummyMessagePrinter.getInstance());
  }
  
  @Test
  public void testPrint() {
    final MessagePrinter instance = DummyMessagePrinter.getInstance();
    instance.print(randomUUID().toString());
  }

  @Test
  public void testPrintln() {
    final MessagePrinter instance = DummyMessagePrinter.getInstance();
    instance.println();
    instance.println(randomUUID().toString());
  }

  @Test
  public void testFlush() throws IOException {
    final MessagePrinter instance = DummyMessagePrinter.getInstance();
    instance.flush();
  }

}