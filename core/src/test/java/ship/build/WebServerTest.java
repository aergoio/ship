package ship.build;

import static org.junit.Assert.assertTrue;

import hera.server.ServerStatus;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;

public class WebServerTest extends AbstractTestCase {

  @Test
  public void testBootAndDown() {
    final ProjectFile projectFile = new ProjectFile();
    final WebServer webServer = new WebServer();
    webServer.setProjectFile(projectFile);
    webServer.setPort(0);
    webServer.boot(true);
    assertTrue(ServerStatus.PROCESSING == webServer.getStatus());
    webServer.down(true);
    assertTrue(ServerStatus.TERMINATED == webServer.getStatus());
  }

}