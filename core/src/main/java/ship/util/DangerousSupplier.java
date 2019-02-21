package ship.util;

public interface DangerousSupplier<T> {
  T get() throws Exception;
}
