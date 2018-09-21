/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web.service;

import static hera.api.Decoder.defaultDecoder;
import static hera.api.Encoder.defaultEncoder;
import static hera.util.HexUtils.dump;
import static hera.util.IoUtils.from;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

import hera.api.AccountOperation;
import hera.api.ContractOperation;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.Authentication;
import hera.api.model.ContractAddress;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInferface;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.api.model.HostnameAndPort;
import hera.client.AergoClient;
import hera.exception.RpcConnectionException;
import hera.exception.RpcException;
import hera.strategy.NettyConnectStrategy;
import hera.util.HexUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import ship.build.web.exception.AergoNodeException;
import ship.build.web.exception.ResourceNotFoundException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.DeploymentResult;
import ship.build.web.model.ExecutionResult;
import ship.build.web.model.QueryResult;
import ship.test.LuaBinary;
import ship.test.LuaCompiler;

@NoArgsConstructor
@Named
public class ContractService extends AbstractService {
  @Getter
  @Value("${project.endpoint}")
  protected String endpoint;

  protected String password = randomUUID().toString();

  protected Account account;

  protected LuaCompiler luaCompiler = new LuaCompiler();

  protected List<DeploymentResult> deployHistory = new ArrayList<>();

  protected Map<String, DeploymentResult> encodedContractTxHash2contractAddresses = new HashMap<>();

  protected synchronized void ensureAccount() {
    if (null != account) {
      return;
    }
    final NettyConnectStrategy connectStrategy = new NettyConnectStrategy();
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);

    try (final AergoClient aergoApi = new AergoClient(connectStrategy.connect(hostnameAndPort))) {
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      logger.trace("Password: {}", password);
      account = accountOperation.create(password).getResult();
      final AccountAddress accountAddress = account.getAddress();
      final Authentication authentication = new Authentication(accountAddress, password);
      accountOperation.unlock(authentication);
      logger.debug("{} unlocked", authentication);
    } catch (final RpcConnectionException ex) {
      throw new AergoNodeException(
          "Fail to connect aergo[" + endpoint + "]. Check your aergo node.", ex);
    } catch (final RpcException ex) {
      throw new AergoNodeException("Fail to deploy contract", ex);
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
    final NettyConnectStrategy connectStrategy = new NettyConnectStrategy();
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);
    logger.debug("Hostname and port: {}", hostnameAndPort);
    try (final AergoClient aergoApi = new AergoClient(connectStrategy.connect(hostnameAndPort))) {
      final byte[] buildResult = buildDetails.getResult().getBytes();
      final LuaBinary luaBinary = luaCompiler.compile(() -> new ByteArrayInputStream(buildResult));
      logger.trace("Successful to compile:\n{}", dump(from(luaBinary.getPayload())));
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxHash contractTransactionHash =
          contractOperation.deploy(account.getAddress(), () -> from(luaBinary.getPayload()))
              .getResult();
      logger.debug("Contract transaction hash: {}", contractTransactionHash);
      final String encodedContractTxHash = contractTransactionHash.getEncodedValue(defaultEncoder);
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
  public ExecutionResult execute(final String encodedContractTxHash, final String functionName,
      final String... args) throws IOException {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final byte[] decoded = from(defaultDecoder.decode(new StringReader(encodedContractTxHash)));
    logger.debug("Decoded contract hash:\n{}", HexUtils.dump(decoded));
    final ContractTxHash contractTxHash = ContractTxHash.of(decoded);
    ensureAccount();
    final NettyConnectStrategy connectStrategy = new NettyConnectStrategy();
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);
    try (final AergoClient client = new AergoClient(connectStrategy.connect(hostnameAndPort))) {
      final ContractOperation contractOperation = client.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash).getResult();
      logger.debug("Receipt: {}", contractTxReceipt);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();
      final DeploymentResult deploymentResult =
          encodedContractTxHash2contractAddresses.get(encodedContractTxHash);
      final ContractFunction contractFunction = deploymentResult.getContractInterface()
          .findFunctionByName(functionName)
          .orElseThrow(() -> new ResourceNotFoundException("No " + functionName + " function."));

      logger.trace("Executing...");
      final ContractTxHash executionContractHash = contractOperation.execute(
          account.getAddress(),
          contractAddress,
          contractFunction,
          stream(args).toArray()
      ).getResult();

      final ExecutionResult executionResult = new ExecutionResult();
      executionResult.setContractTransactionHash(executionContractHash.getEncodedValue());
      return executionResult;
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
  public QueryResult query(final String encodedContractTxHash, final String functionName,
      final String... args) throws IOException {
    logger.trace("Encoded tx hash: {}", encodedContractTxHash);
    final byte[] decoded = from(defaultDecoder.decode(new StringReader(encodedContractTxHash)));
    logger.debug("Decoded contract hash:\n{}", HexUtils.dump(decoded));
    final ContractTxHash contractTxHash = ContractTxHash.of(decoded);
    ensureAccount();
    final NettyConnectStrategy connectStrategy = new NettyConnectStrategy();
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);
    try (final AergoClient client = new AergoClient(connectStrategy.connect(hostnameAndPort))) {
      final ContractOperation contractOperation = client.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash).getResult();
      logger.debug("Receipt: {}", contractTxReceipt);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();
      final DeploymentResult deploymentResult =
          encodedContractTxHash2contractAddresses.get(encodedContractTxHash);
      final ContractFunction contractFunction = deploymentResult.getContractInterface()
          .findFunctionByName(functionName)
          .orElseThrow(() -> new ResourceNotFoundException("No " + functionName + " function."));

      logger.trace("Querying...");
      final ContractResult contractResult = contractOperation.query(
          contractAddress,
          contractFunction,
          stream(args).toArray()
      ).getResult();
      final String resultString = contractResult.getResultInRawBytes().getEncodedValue(in -> {
        try {
          return new InputStreamReader(in);
        } catch (final Throwable ex) {
          throw new IllegalStateException(ex);
        }
      });
      return new QueryResult(resultString);
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
      try {
        final byte[] decoded = from(defaultDecoder.decode(new StringReader(encodedContractTxHash)));
        logger.debug("Decoded contract hash:\n{}", HexUtils.dump(decoded));
        final ContractTxHash contractTxHash = ContractTxHash.of(decoded);
        final ContractInferface contractInferface = getInterface(contractTxHash);
        latest.setContractInterface(contractInferface);
      } catch (IOException ex) {
        throw new ResourceNotFoundException(latest + " not found.");
      }
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
  public ContractInferface getInterface(final ContractTxHash contractTxHash) {
    final NettyConnectStrategy connectStrategy = new NettyConnectStrategy();
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);
    try (final AergoClient client = new AergoClient(connectStrategy.connect(hostnameAndPort))) {
      final ContractOperation contractOperation = client.getContractOperation();
      return contractOperation.getReceipt(contractTxHash)
          .map(ContractTxReceipt::getContractAddress)
          .flatMap(contractOperation::getContractInterface)
          .getOrThrows(() -> new ResourceNotFoundException(
              "Application Binary Interface not found for " + contractTxHash.toString()));
    }
  }
}
