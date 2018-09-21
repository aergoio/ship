package ship.exec;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.util.DummyMessagePrinter;

public class ShipLauncherTest extends AbstractTestCase {

  @Test
  @PrepareForTest({ ShipLauncher.class, System.class })
  public void testRun() {
    final ShipLauncher shipLauncher = spy(new ShipLauncher(DummyMessagePrinter.getInstance()));
    doNothing().when(shipLauncher).exit(anyInt());
    shipLauncher.run(new String[] {});
    verify(shipLauncher).exit(anyInt());
  }
}