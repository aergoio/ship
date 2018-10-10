package ship.exec;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Optional;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.Command;
import ship.CommandFactory;
import ship.exception.BuildException;
import ship.exception.CommandException;
import ship.util.DummyMessagePrinter;

public class ShipLauncherTest extends AbstractTestCase {

  @Test
  @PrepareForTest({ ShipLauncher.class, System.class })
  public void testRun() {
    final ShipLauncher shipLauncher = spy(new ShipLauncher(DummyMessagePrinter.getInstance()));
    doNothing().when(shipLauncher).exit(anyInt());
    shipLauncher.run(new String[] {});
    verify(shipLauncher).exit(anyInt());
  }

  @Test
  @PrepareForTest({ ShipLauncher.class, System.class })
  public void testExecute() {
    final ShipLauncher shipLauncher = spy(new ShipLauncher(DummyMessagePrinter.getInstance()));
    doNothing().when(shipLauncher).exit(anyInt());
    final Command command = mock(Command.class);
    shipLauncher.execute(command);
    verify(shipLauncher).exit(anyInt());
  }

  @Test
  public void shouldExitWithErrorCode() throws Exception {
    final CommandFactory commandFactory = mock(CommandFactory.class);
    final String commandName = randomUUID().toString();
    final Command command = mock(Command.class);
    whenNew(CommandFactory.class).withAnyArguments().thenReturn(commandFactory);
    when(commandFactory.create(any())).thenReturn(Optional.ofNullable(command));
    doThrow(new BuildException(randomUUID().toString())).when(command).execute();
    final ShipLauncher shipLauncher = spy(new ShipLauncher(DummyMessagePrinter.getInstance()));
    doNothing().when(shipLauncher).exit(anyInt());
    shipLauncher.run(new String[] { commandName });
    verify(shipLauncher).exit(-1);
  }
}