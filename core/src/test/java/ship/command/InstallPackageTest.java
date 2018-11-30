/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.nio.file.Files;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.FileSet;
import ship.util.DummyMessagePrinter;

public class InstallPackageTest extends AbstractTestCase {

  @Test
  @PrepareForTest(InstallPackage.class)
  public void testExecute() throws Exception {
    // Given
    final CloneGit cloneGit = mock(CloneGit.class);
    final FileSet fileSet = mock(FileSet.class);
    mockStatic(Files.class);
    when(Files.createDirectories(any())).thenReturn(null);
    whenNew(CloneGit.class).withAnyArguments().thenReturn(cloneGit);
    when(cloneGit.getFileSet()).thenReturn(fileSet);

    // When
    final InstallPackage command = new InstallPackage();
    command.setPrinter(DummyMessagePrinter.getInstance());
    command.setArguments(singletonList(randomUUID().toString() + "/" + randomUUID().toString()));
    command.execute();

    // Then
  }

}