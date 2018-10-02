package ship.util;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.util.HashSet;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;

public class FileWatcherTest extends AbstractTestCase {

  @Test
  @PrepareForTest(File.class)
  public void testRake() {
    // Given
    final File base = mock(File.class);

    // When
    final FileWatcher fileWatcher = new FileWatcher(base);
    final HashSet<File> checked = new HashSet<>();
    final HashSet<File> changed = new HashSet<>();
    fileWatcher.rake(checked, changed);

    // Then
  }

  @Test
  @PrepareForTest(File.class)
  public void testProcess() throws Exception {
    // Given
    final File base = mock(File.class);

    // When
    final FileWatcher fileWatcher = new FileWatcher(base);
    fileWatcher.process();

    // Then
  }
}