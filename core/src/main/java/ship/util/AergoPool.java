package ship.util;

import static hera.util.IoUtils.tryClose;

import hera.api.AergoApi;
import hera.client.AergoClientBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AergoPool implements ResourcePool<AergoApi> {

  @Getter
  protected final String hostnameAndPort;

  @Override
  public AergoApi borrowResource() {
    return new AergoClientBuilder()
        .withEndpoint(hostnameAndPort)
        .build();
  }

  @Override
  public void returnResource(final AergoApi resource) {
    tryClose(resource);
  }

}
