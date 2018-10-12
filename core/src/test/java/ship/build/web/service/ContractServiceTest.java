package ship.build.web.service;

import static hera.util.Base58Utils.encodeWithCheck;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import hera.api.AccountOperation;
import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.BytesValue;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInterface;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.build.web.exception.ResourceNotFoundException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.DeploymentResult;
import ship.build.web.model.ExecutionResult;
import ship.build.web.model.QueryResult;
import ship.test.LuaCompiler;
import ship.util.ResourcePool;

@PrepareForTest(LuaCompiler.class)
public class ContractServiceTest extends AbstractTestCase {

  protected final ContractTxHash contractTxHash =
      ContractTxHash.of(BytesValue.of(randomUUID().toString().getBytes()));

  protected ResourcePool<AergoApi> resourcePool = new ResourcePool<AergoApi>() {
    @Override
    public AergoApi borrowResource() {
      return aergoApi;
    }

    @Override
    public void returnResource(final AergoApi resource) {
    }
  };

  protected ContractService contractService;

  protected Account account = new Account();

  protected ContractTxReceipt contractTxReceipt;

  protected ContractInterface contractInterface;

  protected ContractFunction contractFunction;

  @Mock
  protected AergoApi aergoApi;

  @Mock
  protected AccountOperation accountOperation;

  @Mock
  protected ContractOperation contractOperation;

  @Before
  public void setUp() throws Exception {
    final Runtime runtime = mock(Runtime.class);
    final Process p = mock(Process.class);
    mockStatic(Runtime.class);
    when(Runtime.getRuntime()).thenReturn(runtime);
    when(runtime.exec(anyString(), any(String[].class))).thenReturn(p);
    when(p.getInputStream()).thenReturn(new ByteArrayInputStream(
        encodeWithCheck(randomUUID().toString().getBytes()).getBytes()));
    when(p.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));
    when(p.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    when(p.waitFor()).thenReturn(0);

    contractTxReceipt = new ContractTxReceipt();
    contractInterface = new ContractInterface();
    contractFunction = new ContractFunction();
    contractFunction.setName(randomUUID().toString());
    contractFunction.setArgumentNames(new ArrayList<>());
    contractInterface.setFunctions(asList(contractFunction));

    contractService = new ContractService();
    contractService.setAergoPool(resourcePool);
    reset(aergoApi, contractOperation);
    when(aergoApi.getAccountOperation()).thenReturn(accountOperation);
    when(aergoApi.getContractOperation()).thenReturn(contractOperation);

    when(accountOperation.create(anyString())).thenReturn(account);
    when(accountOperation.get(account)).thenReturn(account);
    when(contractOperation.getReceipt(contractTxHash)).thenReturn(contractTxReceipt);
    when(contractOperation.getContractInterface(contractTxReceipt.getContractAddress())).thenReturn(contractInterface);
  }

  @Test
  public void testDeployAndGetLatestContractInformation() throws Exception {
    // Given
    when(contractOperation.deploy(any(), any(Account.class), anyLong(), any())).thenReturn(contractTxHash);

    // When
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setResult(randomUUID().toString());
    final DeploymentResult result = contractService.deploy(buildDetails);

    // Then
    assertNotNull(result.getEncodedContractTransactionHash());
    assertNotNull(contractService.getLatestContractInformation());
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowException() {
    final ContractService contractService = new ContractService();
    contractService.getLatestContractInformation();
  }

  @Test
  public void testTryExecute() throws Exception {
    when(contractOperation.deploy(any(), any(Account.class), anyLong(), any()))
        .thenReturn(contractTxHash);
    final ContractTxHash executedContractTxHash =
        ContractTxHash.of(BytesValue.of(randomUUID().toString().getBytes()));
    when(contractOperation.execute(any(), any(Account.class), anyLong(), any()))
        .thenReturn(executedContractTxHash);

    // When
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setResult(randomUUID().toString());
    final DeploymentResult result = contractService.deploy(buildDetails);
    result.setContractInterface(contractInterface);
    final ExecutionResult executionResult =
        contractService.tryExecute(contractTxHash.toString(), contractFunction.getName());

    // Then
    assertNotNull(executionResult.getContractTransactionHash());
  }

  @Test
  public void testTryQuery() throws Exception {
    when(contractOperation.deploy(any(), any(Account.class), anyLong(), any())).thenReturn(contractTxHash);
    final ContractResult contractResult = mock(ContractResult.class);
    when(contractResult.getResultInRawBytes())
        .thenReturn(BytesValue.of(randomUUID().toString().getBytes()));
    when(contractOperation.query(any())).thenReturn(contractResult);

    // When
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setResult(randomUUID().toString());
    final DeploymentResult result = contractService.deploy(buildDetails);
    result.setContractInterface(contractInterface);
    final QueryResult queryResult =
        contractService.tryQuery(contractTxHash.toString(), contractFunction.getName());

    // Then
    assertNotNull(queryResult.getResult());
  }
}