package ship.build.web.service.component;

import hera.api.AccountOperation;
import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.encode.Base58WithCheckSum;
import hera.api.model.Account;
import hera.api.model.AccountState;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractTxHash;
import hera.api.model.Fee;
import hera.exception.RpcConnectionException;
import hera.exception.RpcException;
import ship.build.web.exception.AergoNodeException;
import ship.test.LuaBinary;
import ship.util.AergoPool;
import ship.util.Messages;

public interface DeployComponent extends Aergo, LoggerComponent {

  String NL_0 = DeployComponent.class.getName() + ".0";
  String NL_1 = DeployComponent.class.getName() + ".1";

  /**
   * Deploy function fragment.
   *
   * @param account account to deploy
   * @param luaBinary contract to deploy
   *
   * @return transaction's hash to contain contract
   */
  default ContractTxHash deploy(final Account account, final LuaBinary luaBinary) {
    final AergoPool aergoPool = getAergoPool();
    final AergoApi aergoApi = aergoPool.borrowResource();
    final Fee fee = getFee();
    try {
      final AccountOperation accountOperation = aergoApi.getAccountOperation();
      final AccountState syncedAccount = accountOperation.getState(account);
      final ContractOperation contractOperation = aergoApi.getContractOperation();
      final Base58WithCheckSum encodedPayload = () -> luaBinary.getPayload().getEncodedValue();

      final ContractDefinition contractDef =
          ContractDefinition.of(encodedPayload.getEncodedValue());

      return contractOperation.deploy(account, contractDef, syncedAccount.getNonce() + 1, fee);
    } catch (final RpcConnectionException ex) {
      throw new AergoNodeException(Messages.bind(NL_0, aergoPool.getHostnameAndPort()), ex);
    } catch (final RpcException ex) {
      throw new AergoNodeException(Messages.bind(NL_1), ex);
    } finally {
      aergoPool.returnResource(aergoApi);
    }
  }
}
