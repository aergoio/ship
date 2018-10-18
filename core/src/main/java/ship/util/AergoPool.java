package ship.util;

import static hera.util.IoUtils.tryClose;

import hera.api.AergoApi;
import hera.api.model.HostnameAndPort;
import hera.client.AergoClientBuilder;
import hera.strategy.ConnectStrategy;
import hera.strategy.NettyConnectStrategy;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AergoPool implements ResourcePool<AergoApi> {

  protected final ConnectStrategy<ManagedChannel> connectStrategy;

  /**
   * Constructor with endpoint string.
   *
   * @param endpoint endpoint as string
   */
  public AergoPool(final String endpoint) {
    final HostnameAndPort hostnameAndPort = HostnameAndPort.of(endpoint);
    final NettyConnectStrategy nettyConnectStrategy = new NettyConnectStrategy(hostnameAndPort);
    this.connectStrategy = nettyConnectStrategy;
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
