/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.ExceptionUtils.buildExceptionMessage;
import static hera.util.ValidationUtils.assertTrue;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static ship.build.web.model.BuildSummary.BUILD_FAIL;
import static ship.build.web.model.BuildSummary.SUCCESS;
import static ship.build.web.model.BuildSummary.TEST_FAIL;

import hera.util.DangerousConsumer;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import ship.Builder;
import ship.ProjectFile;
import ship.build.ConsoleServer;
import ship.build.Resource;
import ship.build.ResourceChangeEvent;
import ship.build.ResourceManager;
import ship.build.WebServer;
import ship.build.res.BuildResource;
import ship.build.res.PackageResource;
import ship.build.res.Project;
import ship.build.web.model.BuildDetails;
import ship.test.TestReportNode;
import ship.util.FileWatcher;

public class BuildProject extends AbstractCommand {

  protected static final int COMMAND_MODE = 1;
  protected static final int CONSOLE_MODE = 2;
  protected static final int WEB_MODE = 3;

  protected Builder builder;

  protected Project project;

  protected List<DangerousConsumer<BuildDetails>> buildListeners = new ArrayList<>();

  protected FileWatcher createFileWatcher() {
    final FileWatcher fileWatcher = new FileWatcher(project.getPath().toFile());
    fileWatcher.addIgnore(".git");
    fileWatcher.addServerListener(builder.getResourceManager());
    fileWatcher.run();
    return fileWatcher;
  }

  protected void test(final BuildDetails buildDetails) {
    try {
      final TestProject testProject = new TestProject();
      testProject.setBuilderFactory(p -> builder);
      testProject.setReporter(testResultCollector -> {
        final Collection<TestReportNode> testResults = testResultCollector.getResults();
        if (buildDetails.getState() == SUCCESS
            && testResults.stream().anyMatch(testFile -> !testFile.isSuccess())) {
          buildDetails.setState(TEST_FAIL);
        }
        buildDetails.setUnitTestReport(testResults);
      });
      testProject.execute();
    } catch (final Throwable ex) {
      buildDetails.setError(buildExceptionMessage("Error in unit test", ex));
    }
  }

  protected BuildDetails build(final Project project, final boolean runTests) {
    final ProjectFile projectFile = project.getProjectFile();
    final String buildTarget = projectFile.getTarget();
    final BuildDetails buildDetails = new BuildDetails();
    final long startTimestamp = currentTimeMillis();
    if (null == buildTarget) {
      buildDetails.setState(BUILD_FAIL);
    } else {
      try {
        buildDetails.copyFrom(builder.build(buildTarget));
        final String contents = buildDetails.getResult();
        try (final Writer out = Files.newBufferedWriter(project.getPath().resolve(buildTarget))) {
          out.write(contents);
        }
        if (runTests) {
          test(buildDetails);
        }
      } catch (final Throwable buildException) {
        buildDetails.setState(BUILD_FAIL);
        buildDetails.setError(buildException.getMessage());
      }
    }
    final long endTimestamp = currentTimeMillis();
    buildDetails.setElapsedTime(endTimestamp - startTimestamp);

    this.buildListeners.forEach(listener -> {
      try {
        listener.accept(buildDetails);
      } catch (final Throwable ex) {
        logger.trace("Listener {} throws exception", listener, ex);
      }
    });
    return buildDetails;
  }

  protected void startWebServer(final int port) {
    final WebServer webServer = new WebServer(port);
    webServer.setProjectFile(this.project.getProjectFile());
    webServer.boot(true);
    this.buildListeners.add(webServer.getBuildService()::save);
  }

  protected void startConsoleServer() {
    final ConsoleServer consoleServer = new ConsoleServer();
    consoleServer.setPrinter(getPrinter());
    this.buildListeners.add(consoleServer::process);
    consoleServer.boot();
  }

  @Override
  public void execute() throws Exception {
    logger.trace("Starting {}...", this);
    int mode = COMMAND_MODE;
    int port = -1;
    for (int i = 0, n = arguments.size(); i < n; ++i) {
      final String argument = arguments.get(i);
      if ("--watch".equals(argument)) {
        if (mode == COMMAND_MODE) {
          mode = CONSOLE_MODE;
        }
      } else if ("--port".equals(argument)) {
        mode = WEB_MODE;
        assertTrue(++i < arguments.size());
        final String portStr = arguments.get(i);
        port = Integer.parseInt(portStr);
      }
    }

    final ProjectFile projectFile = readProject();
    project = new Project(".", projectFile);
    final ResourceManager resourceManager = new ResourceManager(project);
    builder = new Builder(resourceManager);
    switch (mode) {
      case COMMAND_MODE:
        executeInCommandMode();
        break;
      case WEB_MODE:
        executeInWebMode(port);
        break;
      case CONSOLE_MODE:
        executeInConsoleMode();
        break;
      default:
        throw new IllegalStateException();
    }
  }

  protected void executeInCommandMode() {
    final BuildDetails buildDetails = build(project, false);
    switch (buildDetails.getState()) {
      case SUCCESS:
        getPrinter().println("Successful to build.");
        getPrinter().println("Target: <green>%s</green>",
            project.getProjectFile().getTargetPath(getProjectHome()));
        break;
      case BUILD_FAIL:
        getPrinter().println("<red>Fail to build.</red>");
        final String errorMessage = buildDetails.getError();
        getPrinter().println("Cause: %s", errorMessage);
        break;
      case TEST_FAIL:
        throw new IllegalStateException();
      default:
        throw new IllegalStateException();
    }
  }

  protected void executeInWebMode(final int port) {
    startWebServer(port);
    startConsoleServer();
    build(project, true);
    builder.getResourceManager().addResourceChangeListener(this::resourceChanged);
    createFileWatcher();
  }

  protected void executeInConsoleMode() {
    startConsoleServer();
    build(project, true);
    builder.getResourceManager().addResourceChangeListener(this::resourceChanged);
    createFileWatcher();
  }

  protected void resourceChanged(final ResourceChangeEvent event) {
    logger.info("Resource changed: {}", event);
    final Resource changedResource = event.getResource();
    if (changedResource instanceof PackageResource) {
      logger.trace("Skip package resource: {}", changedResource.getLocation());
      return;
    } else if (changedResource instanceof BuildResource) {
      logger.trace("Skip build resource: {}", changedResource.getLocation());
      return;
    }
    build(project, true);
  }
}
