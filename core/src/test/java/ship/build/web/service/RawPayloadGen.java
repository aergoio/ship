package ship.build.web.service;

import static hera.util.Base58Utils.encodeWithCheck;

import java.util.UUID;

public class RawPayloadGen {
  public byte[] generate() {
    final byte[] rawPayload = UUID.randomUUID().toString().getBytes();
    rawPayload[0] = (byte) 0xC0;
    return rawPayload;
  }

}
