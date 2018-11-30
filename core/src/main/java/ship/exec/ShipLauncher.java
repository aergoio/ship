/*
 * @copyright defined in LICENSE.txt
 */

package ship.exec;

import static org.slf4j.LoggerFactory.getLogger;

import hera.util.ExceptionUtils;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import ship.Command;
import ship.CommandFactory;
import ship.exception.CommandException;
import ship.util.AnsiMessagePrinter;
import ship.util.MessagePrinter;
import ship.util.Messages;

@RequiredArgsConstructor
public class ShipLauncher {
  protected static final Logger logger = getLogger(ShipLauncher.class);

  protected static final String NL_0 = ShipLauncher.class.getName() + ".0";
  protected static final String NL_1 = ShipLauncher.class.getName() + ".1";
  protected static final String NL_2 = ShipLauncher.class.getName() + ".2";
  protected static final String NL_3 = ShipLauncher.class.getName() + ".3";

  /**
   * ship's entry point.
   *
   * @param args user arguments
   */
  public static void main(final String[] args) {
    logger.info("Apm launched");
    final ShipLauncher instance = new ShipLauncher(new AnsiMessagePrinter(System.out));
    instance.run(args);
  }

  protected final MessagePrinter messagePrinter;

  public void exit(int returnCode) {
    System.exit(returnCode);
  }

  /**
   * Create and execute command as user input.
   *
   * @param args user inputs
   */
  public void run(final String[] args) {
    if (args.length < 1) {
      messagePrinter.println(NL_0);
      exit(-1);
    } else {
      final CommandFactory commandFactory = new CommandFactory(messagePrinter);
      final Optional<Command> commandOpt = commandFactory.create(args);
      if (!commandOpt.isPresent()) {
        messagePrinter.println(NL_1, args[0]);
        exit(-1);
      }

      commandOpt.ifPresent(this::execute);
    }
  }

  /**
   * Execute command.
   *
   * <p>
   * Exit process with return code.
   * </p>
   * @param command command to execute
   */
  public void execute(final Command command) {
    try {
      logger.trace("{} starting...", command);
      command.execute();
      exit(0);
    } catch (final CommandException throwable) {
      handleCommandException(command, throwable);
    } catch (final Throwable throwable) {
      handleThrowable(command, throwable);
    }
  }

  protected void handleCommandException(
      final Command command,
      final CommandException commandException) {
    final String message = commandException.getMessage();
    if (null == message) {
      logger.error("Fail to execute {}", command, commandException);
    } else {
      messagePrinter.println(Messages.bind(NL_2, message));
      logger.error("{}", message, commandException);
    }
    exit(-1);
  }

  protected void handleThrowable(final Command command, final Throwable throwable) {
    final String mainMessage = ExceptionUtils.buildExceptionMessage(null, throwable);
    System.err.println(mainMessage);
    System.err.println(NL_3);
    logger.debug("Unexpected exception: {}", throwable.getClass(), throwable);
    exit(-1);
  }
}
