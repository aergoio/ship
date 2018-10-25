package ship.build.web.service.component;

import static java.util.Arrays.stream;

import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.model.ContractAddress;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInvocation;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import ship.util.AergoPool;

public interface QueryComponent extends Aergo, LoggerComponent {

  /**
   * Query the result.
   *
   * @param contractFunction function to query
   * @param contractTxHash transaction hash to query
   * @param args function arguments
   *
   * @return result to query
   */
  default ContractResult query(final ContractFunction contractFunction,
      final ContractTxHash contractTxHash, final String... args) {
    final AergoPool aergoPool = getAergoPool();
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash);
      debug("Receipt: {}", contractTxReceipt);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();

      final ContractInvocation contractCall =
          new ContractInvocation(contractAddress, contractFunction, stream(args).toArray());
      trace("Querying...");
      return contractOperation.query(contractCall);
    } finally {
      aergoPool.returnResource(aergoApi);
    }

  }

}
