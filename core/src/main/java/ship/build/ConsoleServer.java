package ship.build;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import hera.server.AbstractServer;
import hera.server.ServerStatus;
import java.io.IOException;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.BuildSummary;
import ship.test.TestReportNode;
import ship.util.DummyMessagePrinter;
import ship.util.MessagePrinter;

public class ConsoleServer extends AbstractServer {

  protected static final String NL_0 = ConsoleServer.class.getName() + ".0";
  protected static final String NL_1 = ConsoleServer.class.getName() + ".1";
  protected static final String NL_2 = ConsoleServer.class.getName() + ".2";
  protected static final String NL_3 = ConsoleServer.class.getName() + ".3";
  protected static final String NL_4 = ConsoleServer.class.getName() + ".4";
  protected static final String NL_5 = ConsoleServer.class.getName() + ".5";
  protected static final String NL_6 = ConsoleServer.class.getName() + ".6";
  protected static final String NL_7 = ConsoleServer.class.getName() + ".7";
  protected static final String NL_8 = ConsoleServer.class.getName() + ".8";
  protected static final String NL_9 = ConsoleServer.class.getName() + ".9";

  protected static final String CLEAR_SCREEN = "\033[2J";
  protected static final String GO_HOME = "\033[H";

  @Getter
  @Setter
  protected MessagePrinter printer = DummyMessagePrinter.getInstance();

  @Override
  public void boot() {
    changeStatus(ServerStatus.PROCESSING);
  }

  @Override
  public void down() {
    changeStatus(ServerStatus.TERMINATED);
  }

  /**
   * Process build result.
   *
   * @param details build result
   */
  public void process(final BuildDetails details) {
    if (!isStatus(ServerStatus.PROCESSING)) {
      return;
    }

    clearScreen();
    printResult(details);
    printer.println();
    if (BuildSummary.SUCCESS == details.getState() || null == details.getError()) {
      printTest(details);
    } else {
      printer.println(NL_0, details.getError());
    }

    try {
      printer.flush();
    } catch (final IOException e) {
      logger.trace("Ignore exception", e);
    }
  }

  protected void clearScreen() {
    printer.print(CLEAR_SCREEN);
    printer.print(GO_HOME);
  }

  protected void printResult(final BuildSummary summary) {
    final String now = new Date().toString();
    switch (summary.getState()) {
      case BuildSummary.SUCCESS:
        printer.println(NL_1, format("%-30s", summary.getElapsedTime() + " ms elapsed"), now);
        break;
      case BuildSummary.BUILD_FAIL:
        printer.println(NL_2, format("%30s", " "), now);
        break;
      case BuildSummary.TEST_FAIL:
        printer.println(NL_3, format("%30s", " "), now);
        break;
      default:
        throw new IllegalArgumentException("Unknown state: " + summary.getState());
    }
  }

  protected void printTest(final BuildDetails buildDetails) {
    ofNullable(buildDetails).map(BuildDetails::getUnitTestReport)
        .ifPresent(testReport -> testReport.forEach(this::print));
  }

  protected void print(final TestReportNode node) {
    if (node.isSuccess()) {
      printer.println(NL_4, node.getName());
    } else {
      printer.println(NL_5, node.getName());
    }
    node.getChildren().forEach(child -> this.printSuite((TestReportNode) child));
  }

  protected void printSuite(final TestReportNode node) {
    final long nFailures = node.getTheNumberOfFailures();
    final String name = node.getName();
    final int successes = node.getTheNumberOfSuccesses();
    final int runs = node.getTheNumberOfTests();
    if (0 < nFailures) {
      printer.print(NL_6, name, successes, runs);
    } else {
      printer.print(NL_7, name, successes, runs);
    }
    node.getChildren().forEach(child -> this.printCase((TestReportNode) child));
  }

  protected void printCase(final TestReportNode node) {
    if (node.isSuccess()) {
      printer.println(NL_8, node.getName(), (node.getEndTime() - node.getStartTime()));
    } else {
      printer.println(NL_9, node.getName(), node.getResultDetail());
    }
  }
}
