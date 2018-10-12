package ship.util;

import static hera.util.IoUtils.tryClose;

import hera.api.AergoApi;
import hera.client.AergoClient;
import hera.client.AergoClientBuilder;
import hera.strategy.ConnectStrategy;
import hera.strategy.NettyConnectStrategy;
import hera.util.Configuration;
import hera.util.conf.InMemoryConfiguration;
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
    final NettyConnectStrategy nettyConnectStrategy = new NettyConnectStrategy();
    final Configuration configuration = new InMemoryConfiguration();
    configuration.define("endpoint", endpoint);
    nettyConnectStrategy.setConfiguration(configuration);
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
