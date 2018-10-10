package ship.build;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static ship.test.TestReportNodeResult.Failure;
import static ship.test.TestReportNodeResult.Success;

import org.junit.Test;
import org.mockito.internal.verification.AtLeast;
import ship.AbstractTestCase;
import ship.build.web.model.BuildDetails;
import ship.test.TestReportNode;
import ship.util.MessagePrinter;

public class ConsoleServerTest extends AbstractTestCase {
  @Test
  public void testProcess() {
    final TestReportNode<?> totalReport = new TestReportNode<>();
    totalReport.setName(randomUUID().toString());
    final TestReportNode<?> suiteReport = new TestReportNode<>();
    suiteReport.setName(randomUUID().toString());
    final TestReportNode<?> successCaseReport = new TestReportNode<>();
    successCaseReport.setName(randomUUID().toString());
    successCaseReport.setResult(Success);

    final TestReportNode<?> failureCaseReport = new TestReportNode<>();
    failureCaseReport.setName(randomUUID().toString());
    failureCaseReport.setResult(Failure);

    suiteReport.addChild(successCaseReport);
    suiteReport.addChild(failureCaseReport);
    totalReport.addChild(suiteReport);
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.getUnitTestReport().add(totalReport);
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