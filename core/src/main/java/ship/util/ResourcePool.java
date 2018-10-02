package ship.util;

public interface ResourcePool<ResourceT> {
  ResourceT borrowResource();

  void returnResource(ResourceT resource);
}
