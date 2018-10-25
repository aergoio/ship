package ship.build.web.service.component;

import static java.util.Arrays.stream;

import hera.api.AccountOperation;
import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.model.Account;
import hera.api.model.AccountState;
import hera.api.model.ContractAddress;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInvocation;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import ship.util.AergoPool;

public interface ExecuteComponent extends Aergo, LoggerComponent {

  /**
   * Execute contract.
   *
   * @param account account to execute
   * @param contractFunction function to execute
   * @param contractTxHash contract's hash to execute
   * @param args execution arguments
   * @return transaction's hash to contain execution
   */
  default ContractTxHash execute(final Account account,
      final ContractFunction contractFunction,
      final ContractTxHash contractTxHash,
      final String... args) {
    final AergoPool aergoPool = getAergoPool();
    final AergoApi aergoApi = aergoPool.borrowResource();
    try {
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final ContractTxReceipt contractTxReceipt =
          contractOperation.getReceipt(contractTxHash);
      debug("Receipt: {}", contractTxReceipt);
      final AccountState syncedAccount = accountOperation.getState(account);
      final ContractAddress contractAddress = contractTxReceipt.getContractAddress();

      trace("Executing...");
      final ContractInvocation contractCall =
          new ContractInvocation(contractAddress, contractFunction, stream(args).toArray());
      account.setNonce(syncedAccount.getNonce());
      return contractOperation.execute(
          account,
          contractCall,
          getFee()
      );
    } finally {
      aergoPool.returnResource(aergoApi);
    }

  }

}
