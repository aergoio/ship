package ship.build.web.service;

import static java.util.UUID.randomUUID;

public class AddressStringValueGen {
  public static String generate() {
    return ('B' + randomUUID().toString());
  }
}
