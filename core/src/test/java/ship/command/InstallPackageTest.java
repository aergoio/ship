/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.FileSet;
import ship.util.MessagePrinter;

public class InstallPackageTest extends AbstractTestCase {

  @Test
  @PrepareForTest(InstallPackage.class)
  public void testExecute() throws Exception {
    // Given
    final CloneGit cloneGit = mock(CloneGit.class);
    cloneGit.setPrinter(mock(MessagePrinter.class));
    whenNew(CloneGit.class).withAnyArguments().thenReturn(cloneGit);
    when(cloneGit.getFileSet()).thenReturn(new FileSet());

    // When
    final InstallPackage command = new InstallPackage();
    command.setPrinter(mock(MessagePrinter.class));
    command.setArguments(singletonList(randomUUID().toString() + "/" + randomUUID().toString()));
    command.execute();

    // Then
  }

}