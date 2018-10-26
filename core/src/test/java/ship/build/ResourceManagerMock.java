package ship.build;

import static java.util.Arrays.stream;
import static org.junit.Assert.assertNotNull;

import hera.util.FilepathUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ship.build.res.Project;
import ship.build.res.Source;

public class ResourceManagerMock extends ResourceManager {

  interface ResourceDef {
    String getName();

    ResourceDef get(String name);
  }

  static class Directory implements ResourceDef {
    @Getter
    protected final String name;

    protected final HashMap<String, ResourceDef> children = new HashMap<>();

    public Directory(final String name, ResourceDef[] children) {
      this.name = name;
      stream(children).forEach(child -> this.children.put(child.getName(), child));
    }

    @Override
    public ResourceDef get(final String name) {
      final ResourceDef child = children.get(name);
      if (null == child) {
        throw new IllegalArgumentException();
      }
      return child;
    }
  }

  @RequiredArgsConstructor
  static class File implements ResourceDef {
    @Getter
    protected final String name;
    @Getter
    protected final Supplier<InputStream> content;

    public ResourceDef get(final String name) {
      throw new IllegalArgumentException();
    }
  }

  public static Directory dir(String name, ResourceDef... resources) {
    return new Directory(name, resources);
  }

  public static File file(String name, Supplier<InputStream> content) {
    return new File(name, content);
  }

  protected final Directory base;

  public ResourceManagerMock(final Project project, Directory dir) {
    super(project);
    this.base = dir;
  }

  @Override
  public synchronized Resource getResource(final String path) {
    logger.debug("Path: {}", path);
    final String[] fragments = FilepathUtils.getCanonicalFragments(path);
    ResourceDef iter = base;
    int index = 0;
    while (index < fragments.length) {
      assertNotNull(iter);
      final String name = fragments[index];
      logger.debug("Name: {}", name);
      iter = iter.get(name);
      ++index;
    }
    if (iter instanceof Directory) {
    } else if (iter instanceof File) {
      final File file = (File) iter;
      final Source source = new Source(project, null) {
        @Override
        public BufferedReader open() {
          final InputStream in = file.getContent().get();
          assertNotNull(file.getName() + " not found", in);
          return new BufferedReader(new InputStreamReader(in));
        }
      };
      return source;
    }
    throw new IllegalArgumentException();
  }
}
