package ship.command;

import static hera.util.ExceptionUtils.buildExceptionMessage;
import static ship.build.web.model.BuildSummary.SUCCESS;
import static ship.build.web.model.BuildSummary.TEST_FAIL;

import hera.util.DangerousConsumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ship.Builder;
import ship.build.ConsoleServer;
import ship.build.Resource;
import ship.build.ResourceChangeEvent;
import ship.build.ResourceManager;
import ship.build.res.BuildResource;
import ship.build.res.PackageResource;
import ship.build.res.Project;
import ship.build.web.model.BuildDetails;
import ship.test.TestReportNode;
import ship.util.FileWatcher;

public class BuildProjectConsoleMode extends BuildProjectCommandMode {

  protected List<DangerousConsumer<BuildDetails>> buildListeners = new ArrayList<>();

  @Override
  protected BuildDetails build(final Project project) {
    final BuildDetails buildDetails = super.build(project);
    test(buildDetails);
    fireEvent(buildDetails);
    return buildDetails;
  }

  protected void startConsoleServer() {
    final ConsoleServer consoleServer = new ConsoleServer();
    consoleServer.setPrinter(getPrinter());
    buildListeners.add(consoleServer::process);
    consoleServer.boot();
  }

  protected FileWatcher createFileWatcher() {
    final FileWatcher fileWatcher = new FileWatcher(project.getPath().toFile());
    fileWatcher.addIgnore(".git");
    fileWatcher.addServerListener(builder.getResourceManager());
    fileWatcher.run();
    return fileWatcher;
  }

  @Override
  protected void initialize() throws IOException {
    super.initialize();
    final ResourceManager resourceManager = new ResourceManager(project);
    builder = new Builder(resourceManager);
  }

  @Override
  public void execute() throws Exception {
    initialize();
    startConsoleServer();
    this.build(project);
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
    this.build(project);
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

  protected void fireEvent(final BuildDetails buildDetails) {
    this.buildListeners.forEach(listener -> {
      try {
        listener.accept(buildDetails);
      } catch (final Throwable ex) {
        logger.trace("Listener {} throws exception", listener, ex);
      }
    });
  }


}
