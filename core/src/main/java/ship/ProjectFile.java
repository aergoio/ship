/*
 * @copyright defined in LICENSE.txt
 */

package ship;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static hera.DefaultConstants.DEFAULT_ENDPOINT;
import static hera.util.FilepathUtils.append;
import static hera.util.FilepathUtils.getCanonicalForm;
import static java.nio.file.Files.newInputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
public class ProjectFile {

  /**
   * Create {@link ProjectFile} from file base.
   *
   * @param path aergo.json file base
   *
   * @return project file
   *
   * @throws IOException Fail to read file
   */
  public static ProjectFile from(final Path path) throws IOException {
    try (final InputStream in = newInputStream(path)) {
      return ProjectFile.from(in);
    }
  }

  /**
   * Bulid project file from input stream.
   *
   * @param in input stream for content
   *
   * @return project file
   *
   * @throws IOException if fail to read stream
   */
  public static ProjectFile from(final InputStream in) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.configure(ALLOW_COMMENTS, true);
    return mapper.readValue(in, ProjectFile.class);
  }

  @Getter
  @Setter
  protected String name;

  @Getter
  @Setter
  protected String source;

  @Getter
  @Setter
  protected String target;

  public String getTargetPath(final Path project) {
    return getCanonicalForm(append(project.toFile().getAbsolutePath(), target));
  }

  @Getter
  @Setter
  protected List<String> dependencies;

  @Getter
  @Setter
  protected List<String> tests;

  @Getter
  @Setter
  protected String endpoint = DEFAULT_ENDPOINT;

  public String toJson() throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(this);
  }
}
