package ship.build.web.service;

import static java.util.UUID.randomUUID;

import hera.api.model.BytesValue;

public class AddressBytesValueGen {
  protected static final AddressStringValueGen stringValueGen = new AddressStringValueGen();
  public static BytesValue generate() {
    return new BytesValue(stringValueGen.generate().getBytes());
  }
}
