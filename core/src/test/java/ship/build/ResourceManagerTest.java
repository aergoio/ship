package ship.build;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.powermock.api.mockito.PowerMockito.mock;
import static ship.util.FileWatcher.ANY_CHANGED;

import hera.server.Server;
import hera.server.ServerEvent;
import java.io.File;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.res.Project;

public class ResourceManagerTest extends AbstractTestCase {
  @Test
  public void testHandle() {
    final ProjectFile projectFile = new ProjectFile();
    final Project project = new Project(randomUUID().toString(), projectFile);
    final Server server = mock(Server.class);
    final ResourceManager resourceManager = new ResourceManager(project);
    final File changedFile = new File(project.getLocation() + "/" + randomUUID().toString());
    final ServerEvent serverEvent =
        new ServerEvent(server, ANY_CHANGED, singletonList(changedFile));

    resourceManager.handle(serverEvent);
  }

}