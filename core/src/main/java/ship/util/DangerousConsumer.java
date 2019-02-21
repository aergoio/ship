package ship.util;

public interface DangerousConsumer<T> {
  void accept(T input) throws Exception;

}
