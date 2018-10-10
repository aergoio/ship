package ship;

import static java.util.UUID.randomUUID;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.mockito.Mock;
import ship.build.Resource;
import ship.build.ResourceManager;

public class BuilderTest extends AbstractTestCase {

  @Mock
  protected ResourceManager resourceManager;

  @Mock
  protected Resource resource;

  @Test
  public void testBuild() {
    final String resourcePath = randomUUID().toString();
    when(resourceManager.getResource(resourcePath)).thenReturn(resource);
    final Builder builder = new Builder(resourceManager);
    builder.build(resourcePath);
  }
}