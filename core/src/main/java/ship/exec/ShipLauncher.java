/*
 * @copyright defined in LICENSE.txt
 */

package ship.exec;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import ship.Command;
import ship.CommandFactory;
import ship.exception.CommandException;
import ship.util.AnsiMessagePrinter;
import ship.util.MessagePrinter;

@RequiredArgsConstructor
public class ShipLauncher {
  protected static final Logger logger = getLogger(ShipLauncher.class);


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

  protected void printHelp() {
    messagePrinter.println("<red>No command!!</red>");
  }

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
      printHelp();
      exit(-1);
    } else {
      final CommandFactory commandFactory = new CommandFactory(messagePrinter);
      final Optional<Command> commandOpt = commandFactory.create(args);
      if (!commandOpt.isPresent()) {
        printHelp();
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
      final String userMessage = throwable.getUserMessage();
      if (null != userMessage) {
        messagePrinter.println("<bg_red> ERROR </bg_red>: " + userMessage);
        logger.error("Fail to execute {}", command, throwable);
      } else {
        logger.error("{}", throwable.getMessage(), throwable);
      }
      exit(-1);
    } catch (final Throwable throwable) {
      System.err.println("Unexpected exception!! Report the bug to support@aergo.io");
      throwable.printStackTrace();
      exit(-1);
    }
  }
}
