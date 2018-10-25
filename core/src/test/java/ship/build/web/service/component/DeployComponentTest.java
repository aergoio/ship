package ship.build.web.service.component;

import static hera.util.Base58Utils.encodeWithCheck;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

import hera.api.AccountOperation;
import hera.api.AergoApi;
import hera.api.ContractOperation;
import hera.api.model.Account;
import hera.api.model.AccountState;
import hera.api.model.BytesValue;
import hera.api.model.ContractTxHash;
import hera.api.model.Fee;
import hera.api.model.ServerManagedAccount;
import lombok.Getter;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import ship.AbstractTestCase;
import ship.test.LuaBinary;
import ship.util.AergoPool;

public class DeployComponentTest extends AbstractTestCase implements DeployComponent {
  @Getter
  @Mock
  protected AergoPool aergoPool;

  @Mock
  protected AergoApi aergoApi;

  @Getter
  protected Fee fee = new Fee(0, 0);

  @Override
  public Logger logger() {
    return logger;
  }

  @Mock
  protected AccountOperation accountOperation;

  @Mock
  protected ContractOperation contractOperation;

  @Mock
  protected ServerManagedAccount account;

  @Test
  public void testDeploy() {
    final ContractTxHash contractTxHash =
        ContractTxHash.of(BytesValue.of(randomUUID().toString().getBytes()));

    when(aergoPool.borrowResource()).thenReturn(aergoApi);
    when(aergoApi.getAccountOperation()).thenReturn(accountOperation);
    when(aergoApi.getContractOperation()).thenReturn(contractOperation);
    when(accountOperation.getState(any(Account.class))).thenReturn(new AccountState());

    // Given
    when(contractOperation.deploy(any(Account.class), any(), any())).thenReturn(contractTxHash);

    // When
    final LuaBinary luaBinary =
        new LuaBinary(() -> () -> encodeWithCheck(randomUUID().toString().getBytes()));
    final ContractTxHash txHash = deploy(account, luaBinary);

    // Then
    assertNotNull(txHash);
  }
}