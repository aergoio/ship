/*
 * @copyright defined in LICENSE.txt
 */

package ship.build;

import static hera.DefaultConstants.DEFAULT_ENDPOINT;
import static hera.server.ServerStatus.BOOTING;
import static hera.server.ServerStatus.PROCESSING;
import static hera.server.ServerStatus.TERMINATED;
import static hera.server.StateConditionFactory.when;
import static hera.util.StringUtils.nvl;

import hera.server.ServerStatus;
import hera.server.ThreadServer;
import hera.util.ThreadUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ship.ProjectFile;
import ship.build.web.SpringWebLauncher;
import ship.build.web.service.BuildService;

@NoArgsConstructor
public class WebServer extends ThreadServer {

  protected int port = -1;

  @Setter
  protected ProjectFile projectFile;

  protected ConfigurableApplicationContext applicationContext;

  public WebServer(final int port) {
    this.port = port;
  }

  /**
   * Get server port.
   * <p>
   * Return 8080 as default port if not specified.
   * </p>
   *
   * @return server port
   */
  public int getPort() {
    if (port < 0) {
      return 8080;
    } else {
      return port;
    }
  }

  /**
   * Get build service from server.
   *
   * @return build service
   */
  public BuildService getBuildService() {
    if (null == applicationContext) {
      return null;
    }
    return applicationContext.getBean(BuildService.class);
  }

  /**
   * Set server port.
   * <p>
   * Specify negative value if you want default port.
   * </p>
   *
   * @param port server port
   */
  public void setPort(final int port) {
    if (isStatus(ServerStatus.TERMINATED)) {
      this.port = port;
    } else {
      throw new IllegalStateException("Server already started");
    }
  }

  /**
   * {@inheritDoc}.
   *
   * TODO: Make ThreadServer can set ClassLoader for thread
   */
  @Override
  public void boot(final boolean isBlock) {
    if (!state.changeState(BOOTING, when(TERMINATED))) {
      logger.error("{} is not terminated status. status: {}", this, state);

      throw new IllegalStateException();
    }

    thread = new Thread(this, getName());

    // SpringApplicaton uses Thread.currentThread().getContextClassLoader()
    // so we have to set it
    // see also {@link SpringApplication#getSpringFactoriesInstances}.
    final ClassLoader classLoader = getClass().getClassLoader();
    logger.debug("{} runs with class loader: {}", this, classLoader);
    thread.setContextClassLoader(classLoader);

    logger.debug("Starting {}...", this);
    thread.start();

    logger.trace("Staring post-process for boot.");
    postBoot();
    if (isBlock) {
      waitStatus(PROCESSING, TERMINATED);
    }
  }

  @Override
  protected void initialize() throws Exception {
    super.initialize();
    final SpringApplicationBuilder builder = new SpringApplicationBuilder(SpringWebLauncher.class);
    builder.bannerMode(Mode.OFF);
    builder.logStartupInfo(false);

    final Map<String, Object> properties = new HashMap<>();
    if (0 <= port) {
      properties.put("server.port", port);
    }
    properties.put("project.endpoint", nvl(projectFile.getEndpoint(), DEFAULT_ENDPOINT));
    properties.put("project.privatekey", projectFile.getPrivatekey());
    properties.put("project.password", projectFile.getPassword());
    if (!properties.isEmpty()) {
      builder.properties(properties);
    }
    LogManager.getLogManager().reset();
    applicationContext = builder.run();
  }

  @Override
  protected void process() throws Exception {
    super.process();
    ThreadUtils.trySleep(2000);
  }

  @Override
  protected void terminate() {
    try {
      applicationContext.close();;
    } catch (Throwable ex) {
      this.exception = ex;
    }
    super.terminate();
  }
}
