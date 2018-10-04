/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static hera.util.ExceptionUtils.buildExceptionMessage;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ship.build.web.model.BuildSummary.BUILD_FAIL;
import static ship.build.web.model.BuildSummary.SUCCESS;
import static ship.build.web.model.BuildSummary.TEST_FAIL;
import static ship.util.Messages.bind;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Stopwatch;
import hera.util.DangerousConsumer;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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

  protected static final String NL_0 = BuildProject.class.getName() + ".0";
  protected static final String NL_1 = BuildProject.class.getName() + ".1";
  protected static final String NL_2 = BuildProject.class.getName() + ".2";
  protected static final String NL_3 = BuildProject.class.getName() + ".3";
  protected static final String NL_4 = BuildProject.class.getName() + ".4";

  protected static final int COMMAND_MODE = 1;
  protected static final int CONSOLE_MODE = 2;
  protected static final int WEB_MODE = 3;

  protected Builder builder;

  protected Project project;

  @Getter
  @Setter
  protected WriteProjectTarget targetWriter = new WriteProjectTarget();

  protected List<DangerousConsumer<BuildDetails>> buildListeners = new ArrayList<>();

  @Getter
  protected BuildDetails lastBuildResult;


  @ToString
  class Options {
    @Parameter(names = "--watch", description = "Run command as server mode")
    @Getter
    @Setter
    protected boolean watch = false;

    @Parameter(names = "--port", description = "Specify a port for web service")
    @Getter
    @Setter
    protected int port = -1;

    public int getMode() {
      return (0 < port) ? WEB_MODE : (watch) ? CONSOLE_MODE : COMMAND_MODE;
    }
  }

  /**
   * Parse and bind arguments.
   *
   * @return bound object
   */
  protected Options getOptions() {
    final Options options = new Options();
    JCommander.newBuilder().addObject(options).build().parse(arguments.toArray(new String[0]));
    logger.debug("Options: {}", options);
    return options;
  }

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
    logger.debug("Starting build...");
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final ProjectFile projectFile = project.getProjectFile();
    final String buildTarget = projectFile.getTarget();
    final BuildDetails buildDetails = new BuildDetails();
    if (null == buildTarget) {
      buildDetails.setState(BUILD_FAIL);
      buildDetails.setError(bind(NL_4));
    } else {
      try {
        final BuildDetails builderResult = builder.build(buildTarget);
        logger.debug("Builder result: {}", builderResult);
        buildDetails.copyFrom(builderResult);
        final byte[] bytes = buildDetails.getResult().getBytes();
        targetWriter.setContents(() -> new ByteArrayInputStream(bytes));
        targetWriter.execute();
        if (runTests) {
          test(buildDetails);
        }
      } catch (final Throwable buildException) {
        logger.debug("Unexpected exception:", buildException);
        buildDetails.setState(BUILD_FAIL);
        buildDetails.setError(buildException.getMessage());
      }
    }
    buildDetails.setElapsedTime(stopwatch.stop().elapsed(MILLISECONDS));
    fireEvent(buildDetails);
    return buildDetails;
  }

  protected void fireEvent(final BuildDetails buildDetails) {
    this.buildListeners.forEach(listener -> {
      try {
        listener.accept(buildDetails);
      } catch (final Throwable ex) {
        logger.trace("Listener {} throws exception", listener, ex);
      }
    });
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
    final Options options = getOptions();
    final ProjectFile projectFile = readProject();
    project = new Project(".", projectFile);
    targetWriter.setProject(project);
    final ResourceManager resourceManager = new ResourceManager(project);
    builder = new Builder(resourceManager);
    switch (options.getMode()) {
      case COMMAND_MODE:
        this.lastBuildResult = executeInCommandMode();
        break;
      case WEB_MODE:
        executeInWebMode(options.getPort());
        break;
      case CONSOLE_MODE:
        executeInConsoleMode();
        break;
      default:
        throw new IllegalStateException();
    }
  }

  protected BuildDetails executeInCommandMode() {
    logger.debug("Starting command mode...");
    final BuildDetails buildDetails = build(project, false);
    logger.debug("Build result: {}", buildDetails);
    switch (buildDetails.getState()) {
      case SUCCESS:
        getPrinter().println(bind(NL_0));
        getPrinter().println(bind(NL_1, project.getProjectFile().getTargetPath(getProjectHome())));
        break;
      case BUILD_FAIL:
        getPrinter().println(bind(NL_2));
        final String errorMessage = buildDetails.getError();
        getPrinter().println(bind(NL_3, errorMessage));
        break;
      case TEST_FAIL:
        throw new IllegalStateException();
      default:
        throw new IllegalStateException();
    }
    return buildDetails;
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
