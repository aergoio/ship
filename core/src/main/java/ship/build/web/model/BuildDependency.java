package ship.build.web.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ship.build.Resource;

@RequiredArgsConstructor
public class BuildDependency {
  @Getter
  protected final Resource parent;

  @Getter
  @Setter
  protected String name;

  @Getter
  @Setter
  protected List<BuildDependency> children = new ArrayList<>();

  public void add(BuildDependency child) {
    this.children.add(child);
  }
}
