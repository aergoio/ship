/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static ship.util.Messages.bind;

import hera.api.AccountOperation;
import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.encode.Base58;
import hera.api.encode.Base58WithCheckSum;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.Authentication;
import hera.api.model.ContractAddress;
import hera.api.model.ContractCall;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInterface;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.exception.RpcConnectionException;
import hera.exception.RpcException;
import hera.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import ship.build.web.exception.AergoNodeException;
import ship.build.web.exception.ResourceNotFoundException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.DeploymentResult;
import ship.build.web.model.ExecutionResult;
import ship.build.web.model.QueryResult;
import ship.test.LuaBinary;
import ship.test.LuaCompiler;
import ship.util.AergoPool;
import ship.util.ResourcePool;

@NoArgsConstructor
@Named
public class ContractService extends AbstractService {

  protected static final String NL_0 = ContractService.class.getName() + ".0";

  @Getter
  @Setter
  @Value("${project.endpoint}")
  protected String endpoint;

  protected String password = randomUUID().toString();

  protected Account account;

  protected final LuaCompiler luaCompiler = new LuaCompiler();

  protected final List<DeploymentResult> deployHistory = new ArrayList<>();

  protected final Map<String, DeploymentResult> encodedContractTxHash2contractAddresses =
      new HashMap<>();

  @Setter
  protected ResourcePool<AergoApi> aergoPool;

  @RequiredArgsConstructor
  class SimpleBase58 implements Base58 {

    @NonNull
    protected final String encoded;

    @Override
    public String getEncodedValue() {
      return encoded;
    }
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
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      logger.trace("Password: {}", password);
      account = accountOperation.create(password);
      final AccountAddress accountAddress = account.getAddress();
      final Authentication authentication = new Authentication(accountAddress, password);
      accountOperation.unlock(authentication);
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
   *
   * @throws Exception Fail to deploy
   */
  public DeploymentResult deploy(final BuildDetails buildDetails) throws Exception {
    ensureAccount();

    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final byte[] buildResult = buildDetails.getResult().getBytes();
      final LuaBinary luaBinary = luaCompiler.compile(() -> new ByteArrayInputStream(buildResult));
      logger.trace("Successful to compile:\n{}", luaBinary.getPayload());
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      final Account syncedAccount = accountOperation.get(account);
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final Base58WithCheckSum encodedPayload = () -> luaBinary.getPayload().getEncodedValue();

      final ContractTxHash contractTransactionHash =
          contractOperation.deploy(null, account, syncedAccount.getNonce() + 1, encodedPayload);
      logger.debug("Contract transaction hash: {}", contractTransactionHash);
      final String encodedContractTxHash = contractTransactionHash.toString();
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
    } finally {
      aergoPool.returnResource(aergoApi);
    }
  }

  protected Pair<ContractTxHash, ContractFunction> find(
      final String encodedContractTxHash, final String functionName) {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final Base58 encoded = new SimpleBase58(encodedContractTxHash);
    final ContractTxHash contractTxHash = new ContractTxHash(encoded);

    final DeploymentResult deploymentResult =
        encodedContractTxHash2contractAddresses.get(encodedContractTxHash);
    final ContractFunction contractFunction = deploymentResult.getContractInterface()
        .findFunctionByName(functionName)
        .orElseThrow(() -> new ResourceNotFoundException("No " + functionName + " function."));
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
   *
   * @throws IOException Fail to execute
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
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash);
      logger.debug("Receipt: {}", contractTxReceipt);
      final Account syncedAccount = accountOperation.get(account);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();

      logger.trace("Executing...");
      final ContractCall contractCall =
          new ContractCall(contractAddress, contractFunction, asList(args));
      final ContractTxHash executionContractHash = contractOperation.execute(
          null,
          syncedAccount,
          syncedAccount.getNonce(),
          contractCall
      );

      final ExecutionResult executionResult = new ExecutionResult();
      executionResult.setContractTransactionHash(executionContractHash.toString());
      return executionResult;
    } finally {
      aergoPool.returnResource(aergoApi);
    }
  }

  /**
   * Query smart contract.
   *
   * @param encodedContractTxHash contract transaction hash
   * @param functionName          function's name to execute
   * @param args                  function's arguments to execute
   *
   * @return query result
   *
   * @throws IOException Fail to query
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
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash);
      logger.debug("Receipt: {}", contractTxReceipt);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();

      final ContractCall contractCall =
          new ContractCall(contractAddress, contractFunction, asList(args));
      logger.trace("Querying...");
      final ContractResult contractResult = contractOperation.query(contractCall);
      final String resultString = new String(contractResult.getResultInRawBytes().getValue());
      return new QueryResult(resultString);
    } finally {
      aergoPool.returnResource(aergoApi);
    }
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
      final Base58 base58 = new SimpleBase58(encodedContractTxHash);
      final ContractTxHash contractTxHash = new ContractTxHash(base58);
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
      ContractTxReceipt receipt = contractOperation.getReceipt(contractTxHash);
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
