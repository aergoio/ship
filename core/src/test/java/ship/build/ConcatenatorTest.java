package ship.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ship.build.ResourceManagerMock.dir;
import static ship.build.ResourceManagerMock.file;

import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.res.BuildResource;
import ship.build.res.Project;
import ship.build.web.model.BuildDetails;

public class ConcatenatorTest extends AbstractTestCase {

  @Test
  public void testVisit() {
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setName("test/test");
    projectFile.setSource("source.lua");
    projectFile.setTarget("target.lua");
    final Project project = new Project(".", projectFile);
    final String base = "/" + getClass().getName().replace('.', '/') + "/case1/";
    final ResourceManager resourceManager = new ResourceManagerMock(
        project,
        dir(
            ".",
            file("aergo.json", () -> open(base + "aergo.json")),
            file("source.lua", () -> open(base + "source.lua")),
            dir("subdir",
                file("ref1.lua", () -> open(base + "subdir/ref1.lua")),
                file("ref2.lua", () -> open(base + "subdir/ref2.lua"))
            )
        )
    );
    final Concatenator concatenator = new Concatenator(resourceManager);
    final BuildDetails buildDetails =
        concatenator.visit(new BuildResource(project, "target.lua"));
    logger.debug("Build result: {}", buildDetails.getResult());
    final String result = buildDetails.getResult();
    final String fingerprint1 = "ref1fingerprint";
    final String fingerprint2 = "ref2fingerprint";
    assertEquals(1, (result.length() - result.replace(fingerprint1, "").length()) / fingerprint1.length());
    assertEquals(1, (result.length() - result.replace(fingerprint2, "").length()) / fingerprint2.length());
    assertTrue(result.contains("world"));
  }

  @Test
  public void shouldNotBeNull() {
    final ProjectFile projectFile = new ProjectFile();
    projectFile.setName("test/test");
    projectFile.setSource("source.lua");
    projectFile.setTarget("target.lua");
    final Project project = new Project(".", projectFile);
    final String base = "/" + getClass().getName().replace('.', '/') + "/case2/";
    final ResourceManager resourceManager = new ResourceManagerMock(
        project,
        dir(
            ".",
            file("aergo.json", () -> open(base + "aergo.json")),
            file("source.lua", () -> open(base + "source.lua"))
        )
    );
    final Concatenator concatenator = new Concatenator(resourceManager);
    final BuildDetails buildDetails =
        concatenator.visit(new BuildResource(project, "target.lua"));
    logger.debug("Build result: {}", buildDetails.getResult());
    assertNotNull(buildDetails.getResult());
  }

}