package ship.command;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ship.build.WebServer;

@RequiredArgsConstructor
public class BuildProjectWebMode extends BuildProjectConsoleMode {

  protected final int port;

  protected void startWebServer(final int port) {
    final WebServer webServer = new WebServer(port);
    webServer.setProjectFile(this.project.getProjectFile());
    webServer.boot(true);
    buildListeners.add(webServer.getBuildService()::save);
  }

  @Override
  protected void initialize() throws IOException {
    super.initialize();
    startWebServer(port);
  }
}
