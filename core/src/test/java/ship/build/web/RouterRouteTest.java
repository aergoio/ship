package ship.build.web;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import org.junit.Test;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ship.AbstractTestCase;
import ship.build.web.model.ContractInput;

@AutoConfigureMockMvc
@SpringBootTest(classes = RouterRouteTestConfig.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@ActiveProfiles({"test"})
public class RouterRouteTest extends AbstractTestCase {
  @Inject
  protected MockMvc mvc;

  @MockBean
  protected Router router;

  @Test
  public void testGetLatestContract() throws Exception {
    mvc.perform(get("/contract")).andExpect(status().isOk());
  }

  @Test
  public void testGetLatestBuilds() throws Exception {
    mvc.perform(get("/builds")).andExpect(status().isOk());
  }

  @Test
  public void testGetBuild() throws Exception {
    mvc.perform(get("/build/" + randomUUID().toString())).andExpect(status().isOk());
  }

  @Test
  public void testDeploy() throws Exception {
    mvc.perform(post("/build/" + randomUUID().toString() + "/deploy")).andExpect(status().isOk());
  }

  @Test
  public void testExecute() throws Exception {
    final ContractInput input = new ContractInput();
    input.setArguments(new String[] {"123", "4556"});
    mvc.perform(post("/contract/" + randomUUID().toString() + "/" + randomUUID().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(input))
    ).andExpect(status().isOk());
    verify(router).execute(anyString(), anyString(), any());
  }

  @Test
  public void testQuery() throws Exception {
    mvc.perform(get("/contract/" + randomUUID().toString() + "/" + randomUUID().toString())
        .param("arguments", "13", "4544")
    ).andExpect(status().isOk());
    verify(router).query(anyString(), anyString(), any());
  }

}