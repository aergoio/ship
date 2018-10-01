/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.res;

import static hera.util.FilepathUtils.append;
import static hera.util.FilepathUtils.getParentPath;
import static java.lang.Character.isWhitespace;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.slf4j.Logger;
import ship.build.Resource;
import ship.build.ResourceManager;

public class Source extends File {

  protected static final String IMPORT_PREFIX = "import";

  protected static final String[] QUOTES = new String[] { "\"", "'" };

  protected static String fromImport(final String line) {
    if (!line.startsWith(IMPORT_PREFIX) || !isWhitespace(line.charAt(IMPORT_PREFIX.length()))) {
      return null;
    }
    final String literals = line.substring(IMPORT_PREFIX.length()).trim();
    for (final String quote : QUOTES) {
      if (literals.startsWith(quote) && literals.endsWith(quote)) {
        return literals.substring(quote.length(), literals.length() - quote.length()).trim();
      }
    }
    return null;
  }

  class BodyCollector {

    protected boolean isBodyPart = false;

    protected final StringJoiner joiner = new StringJoiner("\n");

    public void add(final String line) {
      if (isBodyPart) {
        joiner.add(line);
      } else {
        final String importStr = fromImport(line);
        if (null == importStr) {
          joiner.add(line);
          isBodyPart = true;
        }
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
      return resourceManager.getResource(append(getParentPath(getLocation()), importPath));
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

}
