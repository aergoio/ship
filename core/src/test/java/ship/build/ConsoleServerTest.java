package ship.build;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.internal.verification.AtLeast;
import ship.AbstractTestCase;
import ship.build.web.model.BuildDetails;
import ship.test.TestReportNode;
import ship.util.MessagePrinter;

public class ConsoleServerTest extends AbstractTestCase {
  @Test
  public void testProcess() {
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.getUnitTestReport().add(new TestReportNode());
    final ConsoleServer consoleServer = new ConsoleServer();
    consoleServer.setPrinter(mock(MessagePrinter.class));
    consoleServer.boot();
    try {
      consoleServer.process(buildDetails);
      verify(consoleServer.getPrinter(), new AtLeast(1)).println(any(), any());
    } finally {
      consoleServer.down();
    }
  }
}