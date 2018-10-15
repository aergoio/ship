/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.ObjectUtils.nvl;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static ship.util.Messages.bind;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import ship.Builder;
import ship.BuilderFactory;
import ship.ProjectFile;
import ship.build.ResourceManager;
import ship.build.res.Project;
import ship.build.web.model.BuildDetails;
import ship.test.AthenaContext;
import ship.test.LuaErrorInformation;
import ship.test.LuaRunner;
import ship.test.LuaSource;
import ship.test.TestReportNode;
import ship.test.TestReportNodeResult;
import ship.test.TestResult;
import ship.test.TestResultCollector;

public class TestProject extends AbstractCommand {

  protected static final String NL_0 = TestProject.class.getName() + ".0";
  protected static final String NL_1 = TestProject.class.getName() + ".1";
  protected static final String NL_2 = TestProject.class.getName() + ".2";
  protected static final String NL_3 = TestProject.class.getName() + ".3";
  protected static final String NL_4 = TestProject.class.getName() + ".4";
  protected static final String NL_5 = TestProject.class.getName() + ".5";
  protected static final String NL_6 = TestProject.class.getName() + ".6";

  @Getter
  @Setter
  protected BuilderFactory builderFactory = project -> new Builder(new ResourceManager(project));

  @Setter
  protected Consumer<TestResultCollector> reporter = (testReporter) -> {
    testReporter.getResults().forEach(testFile -> {
      if (testFile.isSuccess()) {
        getPrinter().println(bind(NL_0, testFile.getName()));
      } else {
        final Optional<String> errorMessage =
            ofNullable(testFile.getResultDetail())
                .map(obj -> (LuaErrorInformation) obj)
                .map(LuaErrorInformation::getMessage);
        if (errorMessage.isPresent()) {
          getPrinter().println(bind(NL_1, testFile.getName(), errorMessage.get()));
        } else {
          getPrinter().println(bind(NL_2, testFile.getName()));
        }
      }
      testFile.getChildren().forEach(child -> {
        final TestReportNode<?> testSuite = (TestReportNode<?>) child;
        final String name = testSuite.getName();
        final int successes = testSuite.getTheNumberOfSuccesses();
        final int runs = testSuite.getTheNumberOfTests();
        if (testSuite.isSuccess()) {
          getPrinter().println(bind(NL_3, name, successes, runs));
        } else {
          getPrinter().println(bind(NL_4, name, successes, runs));
        }
        testSuite.getChildren().forEach(testCase -> {
          if (testCase.isSuccess()) {
            getPrinter().println(bind(NL_5, testCase.getName()));
          } else {
            getPrinter().println(bind(NL_6, testCase.getName(), testCase.getResultDetail()));
          }
        });
      });
    });
  };

  protected void executeTest(final Builder builder, final String testPath) {
    logger.trace("Builder: {}, Test path: {}", builder, testPath);
    final TestResultCollector testReporter = AthenaContext.getContext().getTestReporter();
    final BuildDetails buildDetails = builder.build(testPath);
    final LuaSource executable = new LuaSource(buildDetails.getResult());
    testReporter.clear();

    logger.trace("Executing test...");
    testReporter.start(testPath);
    final TestResult testResult = new LuaRunner().run(executable);
    final TestReportNode<LuaErrorInformation> testFile = testReporter.getCurrentTestFile();
    if (!testResult.isSuccess()) {
      logger.info("{} failed", testFile.getName());
      testFile.setResult(TestReportNodeResult.Failure);
      testFile.setResultDetail(testResult.getError());
    }

    logger.debug("Test {} => {}", testPath, testResult);
    if (!testResult.isSuccess()) {
      final LuaErrorInformation error = testResult.getError();
      final int lineNumber = error.getLineNumber();
      logger.debug("Lua Script:\n{}",
          executable.toString(lineNumber - 5, lineNumber + 5, singletonList(lineNumber)));
    }
    testReporter.end();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws Exception {
    logger.trace("Starting {}...", this);
    final ProjectFile projectFile = readProject();
    final Project project = new Project(".", projectFile);
    final Builder builder = builderFactory.create(project);
    final List<String> testPaths = nvl(projectFile.getTests(), EMPTY_LIST);

    AthenaContext.clear();
    try {
      for (final String testPath : testPaths) {
        executeTest(builder, testPath);
      }
    } finally {
      final AthenaContext context = AthenaContext.getContext();
      final TestResultCollector testResultCollector = context.getTestReporter();
      logger.debug("TestResultCollector: {}", testResultCollector);
      reporter.accept(testResultCollector);
    }
  }
}
