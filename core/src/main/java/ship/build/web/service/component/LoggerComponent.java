package ship.build.web.service.component;

import org.slf4j.Logger;

public interface LoggerComponent {

  Logger logger();

  default void error(final String message, final Object... args) {
    logger().error(message, args);
  }

  default void warn(final String message, final Object... args) {
    logger().warn(message, args);
  }

  default void info(final String message, final Object... args) {
    logger().debug(message, args);
  }

  default void debug(final String message, final Object... args) {
    logger().debug(message, args);
  }

  default void trace(final String message, final Object... args) {
    logger().trace(message, args);
  }
}
