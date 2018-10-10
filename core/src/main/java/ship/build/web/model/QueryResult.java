package ship.build.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class QueryResult {

  @Getter
  @Setter
  protected Object result;

  public QueryResult(final Object result) {
    this.result = result;
  }
}
