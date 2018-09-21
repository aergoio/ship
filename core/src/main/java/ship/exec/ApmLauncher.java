/*
 * @copyright defined in LICENSE.txt
 */

package ship.exec;

import static java.lang.System.exit;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import org.slf4j.Logger;
import ship.Command;
import ship.CommandFactory;
import ship.exception.CommandException;
import ship.util.AnsiMessagePrinter;
import ship.util.MessagePrinter;

public class ApmLauncher {
  protected static final Logger logger = getLogger(ApmLauncher.class);

  protected static void printHelp() {
    System.err.println("No command!");
  }

  /**
   * ship's entry point.
   *
   * @param args user arguments
   */
  public static void main(final String[] args) {
    logger.info("Apm launched");
    if (args.length < 1) {
      printHelp();
      exit(-1);
    }
    final MessagePrinter messagePrinter = new AnsiMessagePrinter(System.out);
    final CommandFactory commandFactory = new CommandFactory(messagePrinter);
    final Optional<Command> commandOpt = commandFactory.create(args);
    if (!commandOpt.isPresent()) {
      printHelp();
      exit(-1);
    }

    commandOpt.ifPresent(command -> {
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
    });
  }
}
