/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ship.build.web.model.BuildDetails;

public class BuildProject extends AbstractCommand {

  protected static final int COMMAND_MODE = 1;
  protected static final int CONSOLE_MODE = 2;
  protected static final int WEB_MODE = 3;

  @Getter
  @Setter
  protected Function<Options, BuildProjectCommandMode> commandFactory = options -> {
    switch (options.getMode()) {
      case COMMAND_MODE:
        return new BuildProjectCommandMode();
      case WEB_MODE:
        return new BuildProjectWebMode(options.getPort());
      case CONSOLE_MODE:
        return new BuildProjectConsoleMode();
      default:
        throw new IllegalStateException();
    }
  };

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


  @Override
  public void execute() throws Exception {
    logger.trace("Starting {}...", this);
    final Options options = getOptions();
    final BuildProjectCommandMode buildProjectCommandMode = commandFactory.apply(options);
    buildProjectCommandMode.setPrinter(getPrinter());
    buildProjectCommandMode.execute();
    this.lastBuildResult = buildProjectCommandMode.getLastBuildResult();
  }
}
