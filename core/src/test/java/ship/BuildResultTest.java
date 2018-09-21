/*
 * @copyright defined in LICENSE.txt
 */
package ship;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ship.build.web.model.BuildDetails;

public class BuildResultTest {
  @Test
  public void testJson() throws JsonProcessingException {
    final BuildDetails buildDetails = new BuildDetails();
    final String json = new ObjectMapper().writeValueAsString(buildDetails);
    assertNotNull(json);
  }

}