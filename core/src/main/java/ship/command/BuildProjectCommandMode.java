package ship.command;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ship.build.web.model.BuildSummary.BUILD_FAIL;
import static ship.build.web.model.BuildSummary.SUCCESS;
import static ship.build.web.model.BuildSummary.TEST_FAIL;

import com.google.common.base.Stopwatch;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import ship.Builder;
import ship.BuilderFactory;
import ship.ProjectFile;
import ship.build.ResourceManager;
import ship.build.res.Project;
import ship.build.web.model.BuildDetails;

public class BuildProjectCommandMode extends AbstractCommand {

  protected static final String NL_0 = BuildProject.class.getName() + ".0";
  protected static final String NL_1 = BuildProject.class.getName() + ".1";
  protected static final String NL_2 = BuildProject.class.getName() + ".2";
  protected static final String NL_3 = BuildProject.class.getName() + ".3";
  protected static final String NL_4 = BuildProject.class.getName() + ".4";

  protected Project project;

  @Getter
  @Setter
  protected BuilderFactory builderFactory = project -> new Builder(new ResourceManager(project));

  protected Builder builder;

  @Getter
  protected BuildDetails lastBuildResult;

  @Getter
  @Setter
  protected WriteProjectTarget targetWriter = new WriteProjectTarget();

  protected BuildDetails build(final Project project) {
    logger.debug("Starting build...");
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final ProjectFile projectFile = project.getProjectFile();
    final String buildTarget = projectFile.getTarget();
    final BuildDetails buildDetails = new BuildDetails();
    if (null == buildTarget) {
      buildDetails.setState(BUILD_FAIL);
      buildDetails.setError(NL_4);
    } else {
      try {
        final BuildDetails builderResult = builder.build(buildTarget);
        logger.debug("Builder result: {}", builderResult);
        buildDetails.copyFrom(builderResult);
        final String buildResult = buildDetails.getResult();
        final byte[] bytes = buildResult.getBytes();
        targetWriter.setContents(() -> new ByteArrayInputStream(bytes));
        targetWriter.execute();
      } catch (final Throwable buildException) {
        logger.debug("Unexpected exception:", buildException);
        buildDetails.setState(BUILD_FAIL);
        buildDetails.setError(buildException.getMessage());
      }
    }
    buildDetails.setElapsedTime(stopwatch.stop().elapsed(MILLISECONDS));
    lastBuildResult = buildDetails;
    return buildDetails;
  }

  protected void initialize() throws IOException {
    final ProjectFile projectFile = readProject();
    project = new Project(".", projectFile);
    builder = builderFactory.create(project);
    targetWriter.setProject(project);
  }

  @Override
  public void execute() throws Exception {
    initialize();
    final BuildDetails buildDetails = build(project);
    handle(buildDetails);
    this.lastBuildResult = buildDetails;
  }

  protected void handle(BuildDetails result) {
    logger.debug("Build result: {}", result);
    switch (result.getState()) {
      case SUCCESS:
        getPrinter().println(NL_0);
        getPrinter().println(NL_1, project.getProjectFile().getTargetPath(getProjectHome()));
        break;
      case BUILD_FAIL:
        getPrinter().println(NL_2);
        final String errorMessage = result.getError();
        getPrinter().println(NL_3, errorMessage);
        break;
      case TEST_FAIL:
        throw new IllegalStateException();
      default:
        throw new IllegalStateException();
    }
  }
}
