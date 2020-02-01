/*
 * @copyright defined in LICENSE.txt
 */

package ship.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import ship.bootstrap.UrlFinder;
import ship.bootstrap.UrlFinderFactory;
import ship.bootstrap.CompositeUrlFinder;
import ship.bootstrap.Loader;
import ship.ship.command.LaunchCommand;
import ship.ship.util.IoUtils;

public class LoaderLauncher {
  protected static final String PROP_DEBUG = "ship.bootstrap.verbose";
  protected static final String PROP_LIB = "ship.lib";
  protected static final String PROP_CONF = "ship.conf";

  /**
   * Starting entry point of program.
   *
   * @param args user arguments
   *
   * @throws Exception if unexpected exception occurred
   */
  public static void main(String[] args) throws Exception {
    new LoaderLauncher().run(args);
  }

  /**
   * Build class finder.
   *
   * @param confDir conf directory path
   * @param libDir lib directory path
   * @param debug print debug message or not
   *
   * @return class finder
   */
  protected UrlFinder buildFinder(final String confDir, final String libDir, final boolean debug) {
    final UrlFinderFactory factory = new UrlFinderFactory();
    factory.setDebug(debug);
    final List<UrlFinder> finders = new ArrayList<>();
    Optional.ofNullable(confDir)
        .map(File::new)
        .map(factory::create)
        .ifPresent(finders::add);
    Optional.ofNullable(libDir)
        .map(File::new)
        .map(IoUtils::rake)
        .orElseGet(Stream::empty)
        .map(factory::create)
        .forEach(finders::add);
    final CompositeUrlFinder finder = new CompositeUrlFinder(finders);
    finder.setDebug(debug);
    return finder;
  }

  /**
   * Run ShipLauncher with args.
   *
   * @param args user arguments
   *
   * @throws Exception if unexpected exception occurred
   */
  public void run(final String[] args) throws Exception {
    final String libDir = System.getProperty(PROP_LIB);
    final String confDir = System.getProperty(PROP_CONF);
    final boolean debug = Boolean.parseBoolean(System.getProperty(PROP_DEBUG));
    final Loader loader = new Loader(buildFinder(confDir, libDir, debug));
    loader.setDebug(debug);
    new LaunchCommand(loader, "ship.exec.ShipLauncher", args).execute();
  }
}
