/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({
    "javax.crypto.*",
    "javax.management.*",
    "javax.net.ssl.*",
    "javax.security.*"})
public abstract class AbstractTestCase {

  protected final transient Logger logger = getLogger(getClass());

  protected InputStream open(final String path) {
    logger.trace("Path: {}", path);
    return getClass().getResourceAsStream(path);
  }

  protected InputStream openWithExtensionAs(final String ext) {
    final String path = "/" + getClass().getName().replace('.', '/') + "." + ext;
    return open(path);
  }

}
