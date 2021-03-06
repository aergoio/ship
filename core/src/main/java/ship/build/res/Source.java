/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import static hera.util.FilepathUtils.append;
import static hera.util.FilepathUtils.getParentPath;
import static hera.util.ObjectUtils.equal;
import static java.lang.Character.isWhitespace;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.slf4j.Logger;
import ship.build.Resource;
import ship.build.ResourceManager;

public class Source extends File {

  protected static final String IMPORT_PREFIX = "import";

  protected static final String[] QUOTES = new String[] { "\"", "'" };

  protected static Optional<String> extractLiteralPart(final String line) {
    if (!line.startsWith(IMPORT_PREFIX) || !isWhitespace(line.charAt(IMPORT_PREFIX.length()))) {
      return empty();
    }
    return of(line.substring(IMPORT_PREFIX.length()).trim());
  }

  protected static String fromImport(final String line) {
    return extractLiteralPart(line).flatMap(literal -> {
      for (final String quote : QUOTES) {
        if (literal.startsWith(quote) && literal.endsWith(quote)) {
          return of(literal.substring(quote.length(), literal.length() - quote.length()).trim());
        }
      }
      return empty();
    }).orElse(null);
  }

  class BodyCollector {

    protected boolean isBodyPart = false;

    protected final StringJoiner joiner = new StringJoiner("\n");

    public void add(final String line) {
      if (isBodyPart) {
        joiner.add(line);
      } else if (null == fromImport(line)) {
        joiner.add(line);
        isBodyPart = true;
      }
    }

    @Override
    public String toString() {
      return joiner.toString();
    }
  }

  protected final transient Logger logger = getLogger(getClass());

  public Source(final Project project, final String location) {
    super(project, location);
  }

  @Override
  public List<Resource> getDependencies(final ResourceManager resourceManager) throws Exception {
    final ArrayList<Resource> dependencies = new ArrayList<>();
    dependencies.addAll(super.getDependencies(resourceManager));
    readImports().stream()
        .map(importPath -> this.bind(resourceManager, importPath))
        .forEach(dependencies::add);
    return dependencies;
  }

  protected List<String> readNextImports(final BufferedReader bufferedReader) throws IOException {
    final String line = bufferedReader.readLine();
    if (null == line) {
      return new ArrayList<>();
    }
    if (line.trim().isEmpty()) {
      return readNextImports(bufferedReader);
    }
    final String importStr = fromImport(line.trim());
    if (null == importStr) {
      return new ArrayList<>();
    }
    final List<String> results = readNextImports(bufferedReader);
    results.add(importStr);
    return results;
  }

  /**
   * Read import clause and convert to import target.
   *
   * @return import target list
   *
   * @throws IOException Fail to read source file.
   */
  public List<String> readImports() throws IOException {
    final List<String> imports = new ArrayList<>();
    try (final BufferedReader sourceIn = open()) {
      // Read line by line
      return readNextImports(sourceIn);
    } catch (final NoSuchFileException e) {
      logger.trace("{} not found", location);
      return emptyList();
    }
  }

  protected Resource bind(final ResourceManager resourceManager, final String importPath) {
    if (importPath.startsWith("./") || importPath.startsWith("../")) {
      final String location = getLocation();
      logger.trace("Location: {}", location);
      final String parent = getParentPath(getLocation());
      logger.trace("Parent: {}", parent);
      return resourceManager.getResource(append(parent, importPath));
    } else {
      return resourceManager.getPackage(importPath);
    }
  }

  /**
   * Return text without upper import part.
   *
   * @return body text
   */
  public Text getBody() {
    return new Text(() -> {
      final BodyCollector collector = new BodyCollector();
      try (final BufferedReader sourceIn = open()) {
        // Read line by line
        String line = null;
        while (null != (line = sourceIn.readLine())) {
          logger.trace("Line: {}", line);
          collector.add(line);
        }
        return new ByteArrayInputStream(collector.toString().getBytes());
      }
    });
  }

  @Override
  public int hashCode() {
    return project.hashCode() + location.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Source) {
      final Source other = (Source) obj;
      return equal(project, other.project) && equal(location, other.location);
    }
    return false;
  }
}
