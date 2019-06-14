package ship.ship.command;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LaunchCommand {
  protected final ClassLoader loader;
  protected final String className;
  protected final String[] args;

  /**
   * Execute the entry point.
   *
   * @throws Exception if execution failed
   */
  public void execute() throws Exception {
    final Class<?> clazz = loader.loadClass(className);
    final Method method = clazz.getMethod("main", String[].class);
    method.invoke(null, (Object) args);
  }
}
