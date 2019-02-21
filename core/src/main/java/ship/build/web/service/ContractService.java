/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.service;

import static hera.util.ValidationUtils.assertEquals;
import static hera.util.ValidationUtils.assertNotNull;
import static java.util.UUID.randomUUID;
import static ship.util.Messages.bind;

import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.KeyStoreOperation;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.AccountFactory;
import hera.api.model.Aer;
import hera.api.model.Authentication;
import hera.api.model.ContractAddress;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInterface;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.api.model.Fee;
import hera.exception.RpcConnectionException;
import hera.exception.RpcException;
import hera.key.AergoKey;
import hera.util.Pair;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import ship.build.web.exception.AergoNodeException;
import ship.build.web.exception.ResourceNotFoundException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.DeploymentResult;
import ship.build.web.model.ExecutionResult;
import ship.build.web.model.QueryResult;
import ship.build.web.service.component.DeployComponent;
import ship.build.web.service.component.ExecuteComponent;
import ship.build.web.service.component.QueryComponent;
import ship.test.LuaBinary;
import ship.test.LuaCompiler;
import ship.util.AergoPool;

@NoArgsConstructor
@Named
public class ContractService extends AbstractService
    implements DeployComponent, ExecuteComponent, QueryComponent {

  protected static final String NL_0 = ContractService.class.getName() + ".0";

  @Getter
  @Setter
  @Value("${project.endpoint}")
  protected String endpoint;

  @Getter
  @Setter
  @Value("${project.privatekey:}")
  protected String encodedEncryptedPrivateKey;

  @Getter
  @Setter
  @Value("${project.password:}")
  protected String password;

  protected Account account;

  @Getter
  protected Fee fee = new Fee(Aer.GIGA_ONE, -1);

  protected final LuaCompiler luaCompiler = new LuaCompiler();

  protected final List<DeploymentResult> deployHistory = new ArrayList<>();

  protected final Map<String, DeploymentResult> encodedContractTxHash2contractAddresses =
      new HashMap<>();

  @Setter
  @Getter
  protected AergoPool aergoPool;

  @Override
  public Logger logger() {
    return logger;
  }

  protected synchronized void ensureAccount() {
    if (null != account) {
      return;
    }

    if (null == aergoPool) {
      aergoPool = new AergoPool(endpoint);
    }
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final KeyStoreOperation keyStoreOp = aergoApi.getKeyStoreOperation();
      logger.trace("Password: {}", password);
      if (null == encodedEncryptedPrivateKey || encodedEncryptedPrivateKey.isEmpty()) {
        password = randomUUID().toString();
        account = keyStoreOp.create(password);
      } else {
        try {
          final AergoKey pk = AergoKey.of(encodedEncryptedPrivateKey, password);
          account = new AccountFactory().create(pk);
        } catch (final Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
      final AccountAddress accountAddress = account.getAddress();
      final Authentication authentication = new Authentication(accountAddress, password);
      keyStoreOp.unlock(authentication);
      logger.debug("{} unlocked", authentication);
    } catch (final RpcConnectionException ex) {
      throw new AergoNodeException(
          "Fail to connect aergo[" + endpoint + "]. Check your aergo node.", ex);
    } catch (final RpcException ex) {
      throw new AergoNodeException("Fail to deploy contract", ex);
    } finally {
      aergoPool.returnResource(aergoApi);
    }

  }

  /**
   * Deploy {@code buildDetails}'s result.
   *
   * @param buildDetails build result
   *
   * @return deployment result
   */
  public DeploymentResult deploy(final BuildDetails buildDetails) {
    ensureAccount();

    try {
      final byte[] buildResult = buildDetails.getResult().getBytes();
      final LuaBinary luaBinary = luaCompiler.compile(() -> new ByteArrayInputStream(buildResult));
      logger.trace("Successful to compile:\n{}", luaBinary.getPayload());

      final ContractTxHash contractTxHash =  deploy(account, luaBinary);
      logger.debug("Contract transaction hash: {}", contractTxHash);
      final String encodedContractTxHash = contractTxHash.toString();
      final DeploymentResult deploymentResult = new DeploymentResult();
      deploymentResult.setBuildUuid(buildDetails.getUuid());
      deploymentResult.setEncodedContractTransactionHash(encodedContractTxHash);
      encodedContractTxHash2contractAddresses.put(encodedContractTxHash, deploymentResult);
      deployHistory.add(deploymentResult);
      return deploymentResult;
    } catch (final RpcConnectionException ex) {
      throw new AergoNodeException(
          "Fail to connect aergo[" + endpoint + "]. Check your aergo node.", ex);
    } catch (final RpcException ex) {
      throw new AergoNodeException("Fail to deploy contract", ex);
    }
  }

  protected Pair<ContractTxHash, ContractFunction> find(
      final String encodedContractTxHash, final String functionName) {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final ContractTxHash contractTxHash = new ContractTxHash(encodedContractTxHash);

    final DeploymentResult deploymentResult =
        encodedContractTxHash2contractAddresses.get(encodedContractTxHash);
    final ContractInterface contractInterface = deploymentResult.getContractInterface();
    final ContractFunction contractFunction = contractInterface.findFunction(functionName);
    assertNotNull(contractFunction,
        new ResourceNotFoundException("No " + functionName + " function."));
    return new Pair<>(contractTxHash, contractFunction);
  }

  /**
   * Execute smart contract.
   *
   * @param encodedContractTxHash contract transaction hash
   * @param functionName          function's name to execute
   * @param args                  function's arguments to execute
   *
   * @return execution result
   */
  public ExecutionResult tryExecute(final String encodedContractTxHash, final String functionName,
      final String... args) {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final Pair<ContractTxHash, ContractFunction> pair = find(encodedContractTxHash, functionName);
    return execute(pair.v1, pair.v2, args);
  }

  protected ExecutionResult execute(
      final ContractTxHash contractTxHash,
      final ContractFunction contractFunction,
      final String... args) {
    ensureAccount();
    final ContractTxHash executionContractHash =
        execute(account, contractFunction, contractTxHash, args);
    final ExecutionResult executionResult = new ExecutionResult();
    executionResult.setContractTransactionHash(executionContractHash.toString());
    return executionResult;
  }

  /**
   * Query smart contract.
   *
   * @param encodedContractTxHash contract transaction hash
   * @param functionName          function's name to execute
   * @param args                  function's arguments to execute
   *
   * @return query result
   */
  public QueryResult tryQuery(final String encodedContractTxHash, final String functionName,
      final String... args) {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final Pair<ContractTxHash, ContractFunction> pair = find(encodedContractTxHash, functionName);
    return query(pair.v1, pair.v2, args);
  }

  protected QueryResult query(
      final ContractTxHash contractTxHash,
      final ContractFunction contractFunction,
      final String... args) {
    ensureAccount();
    final ContractResult contractResult = query(contractFunction, contractTxHash, args);
    final String resultString = new String(contractResult.getResultInRawBytes().getValue());
    return new QueryResult(resultString);
  }

  /**
   * Get latest contract.
   *
   * @return latest deployed contract
   */
  public DeploymentResult getLatestContractInformation() {
    if (deployHistory.isEmpty()) {
      throw new ResourceNotFoundException("No deployment!! Deploy your contract first.");
    }
    final DeploymentResult latest = deployHistory.get(deployHistory.size() - 1);
    logger.debug("Latest deployment: {}", latest);
    if (null == latest.getContractInterface()) {
      final String encodedContractTxHash = latest.getEncodedContractTransactionHash();
      logger.trace("Encoded tx hash: {}", encodedContractTxHash);
      final ContractTxHash contractTxHash = new ContractTxHash(encodedContractTxHash);
      final ContractInterface contractInterface = getInterface(contractTxHash);
      latest.setContractInterface(contractInterface);
    }
    return latest;
  }

  /**
   * Get application blockchain interface for {@code encodedContractTransactionHash}
   * from {@code endpoint}.
   *
   * @param contractTxHash contract's transaction hash
   *
   * @return abi set
   */
  public ContractInterface getInterface(final ContractTxHash contractTxHash) {
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxReceipt receipt = contractOperation.getReceipt(contractTxHash);
      assertEquals("CREATED", receipt.getStatus());

      final ContractAddress address = receipt.getContractAddress();
      final ContractInterface contractInterface = contractOperation.getContractInterface(address);
      if (null == contractInterface) {
        throw new ResourceNotFoundException(bind(NL_0, contractTxHash));
      }
      return contractInterface;
    } finally {
      aergoPool.returnResource(aergoApi);
    }
  }
}
