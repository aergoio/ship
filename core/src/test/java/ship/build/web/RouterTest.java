package ship.build.web;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.web.context.request.WebRequest;
import ship.AbstractTestCase;
import ship.build.web.exception.HttpException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.ContractInput;
import ship.build.web.service.BuildService;
import ship.build.web.service.ContractService;
import ship.build.web.service.LiveUpdateService;

public class RouterTest extends AbstractTestCase {
  protected Router router;

  @Mock
  protected BuildService buildService;

  @Mock
  protected ContractService contractService;

  @Mock
  protected LiveUpdateService liveUpdateService;

  @Before
  public void setUp() {
    router = new Router();
    router.setBuildService(buildService);
    router.setContractService(contractService);
    router.setLiveUpdateService(liveUpdateService);
    router.initialize();
  }

  @Test
  public void testGetLatestContract() {
    router.getLatestContract();
    verify(contractService).getLatestContractInformation();
  }

  @Test
  public void testGetLatestBuilds() {
    router.getLatestBuilds();
    verify(buildService).list(any(), anyInt());
  }

  @Test
  public void testGetBuild() {
    final String buildUuid = randomUUID().toString();
    when(buildService.get(anyString())).thenReturn(ofNullable(new BuildDetails()));
    router.getBuild(buildUuid);
  }

  @Test
  public void testDeploy() throws Exception {
    final String buildUuid = randomUUID().toString();
    when(buildService.get(anyString())).thenReturn(ofNullable(new BuildDetails()));
    router.deploy(buildUuid);
    verify(contractService).deploy(any());
  }

  @Test
  public void testExecute() throws Exception {
    final String buildUuid = randomUUID().toString();
    final String functionName = randomUUID().toString();
    router.execute(buildUuid, functionName, new ContractInput());
    verify(contractService).tryExecute(anyString(), anyString(), any(String[].class));
  }

  @Test
  public void testQuery() throws Exception {
    final String buildUuid = randomUUID().toString();
    final String functionName = randomUUID().toString();
    router.query(buildUuid, functionName, new String[0]);
    verify(contractService).tryQuery(anyString(), anyString(), any(String[].class));
  }

  @Test
  public void testHandleHttpException() {
    final HttpException httpException = new HttpException(500);
    final WebRequest webRequest = mock(WebRequest.class);
    assertNotNull(router.handleHttpException(httpException, webRequest));
  }

  @Test
  public void testHandleThrowable() {
    final RuntimeException runtimeException = new RuntimeException();
    final WebRequest webRequest = mock(WebRequest.class);
    assertNotNull(router.handleThrowable(runtimeException, webRequest));
  }

}