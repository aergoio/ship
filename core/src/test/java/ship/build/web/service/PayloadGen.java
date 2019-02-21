package ship.build.web.service;

import static hera.util.Base58Utils.encodeWithCheck;

public class PayloadGen {
  protected final RawPayloadGen rawPayloadGen = new RawPayloadGen();
  public String generate() {
    return encodeWithCheck(rawPayloadGen.generate());
  }

}
