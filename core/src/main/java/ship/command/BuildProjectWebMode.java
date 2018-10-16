package ship.command;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ship.build.WebServer;
import ship.build.web.service.BuildService;
import ship.exception.CommandException;

@RequiredArgsConstructor
public class BuildProjectWebMode extends BuildProjectConsoleMode {

  protected final int port;

  protected void startWebServer(final int port) {
    final WebServer webServer = new WebServer(port);
    webServer.setProjectFile(this.project.getProjectFile());
    webServer.boot(true);
    final Throwable webServerError = webServer.getException();
    if (null == webServerError) {
      final BuildService buildService = webServer.getBuildService();
      buildListeners.add(buildService::save);
    } else {
      if (webServerError instanceof CommandException) {
        throw (CommandException) webServerError;
      } else {
        throw new CommandException(webServerError);
      }
    }
  }

  @Override
  protected void initialize() throws IOException {
    super.initialize();
    startWebServer(port);
  }
}
