package ship.build.web.service;

import hera.api.model.AccountAddress;

public class AccountAddressGen {
  protected static final AddressBytesValueGen baseGen = new AddressBytesValueGen();
  public static AccountAddress generate() {
    return new AccountAddress(baseGen.generate());
  }
}
