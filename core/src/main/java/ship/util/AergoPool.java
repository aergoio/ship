package ship.util;

import static hera.util.IoUtils.tryClose;

import hera.api.AergoApi;
import hera.api.model.HostnameAndPort;
import hera.client.AergoClientBuilder;
import hera.strategy.ConnectStrategy;
import hera.strategy.NettyConnectStrategy;
import io.grpc.ManagedChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AergoPool implements ResourcePool<AergoApi> {

  @Getter
  protected final HostnameAndPort hostnameAndPort;

  protected final ConnectStrategy<ManagedChannel> connectStrategy;

  /**
   * Constructor with endpoint string.
   *
   * @param endpoint endpoint as string
   */
  public AergoPool(final String endpoint) {
    this(HostnameAndPort.of(endpoint));
  }

  public AergoPool(final HostnameAndPort hostnameAndPort) {
    this(hostnameAndPort, new NettyConnectStrategy(hostnameAndPort));
  }

  public AergoPool(final ConnectStrategy<ManagedChannel> connectStrategy) {
    this(null, connectStrategy);
  }

  @Override
  public AergoApi borrowResource() {
    return new AergoClientBuilder()
        .addStrategy(connectStrategy)
        .build();
  }

  @Override
  public void returnResource(final AergoApi resource) {
    tryClose(resource);
  }

}
