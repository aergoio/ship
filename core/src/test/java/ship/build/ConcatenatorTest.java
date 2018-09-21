package ship.build;

import static org.junit.Assert.assertTrue;

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
    final String base = "/" + getClass().getName().replace('.', '/') + "/";
    final ResourceManager resourceManager = new ResourceManagerMock(
        project,
        ResourceManagerMock.dir(
            ".",
            ResourceManagerMock.file("aergo.json", () -> open(base + "aergo.json")),
            ResourceManagerMock.file("source.lua", () -> open(base + "source.lua")),
            ResourceManagerMock.dir("subdir",
                ResourceManagerMock.file("ref1.lua", () -> open(base + "subdir/ref1.lua"))
            )
        )
    );
    final Concatenator concatenator = new Concatenator(resourceManager);
    final BuildDetails buildDetails =
        concatenator.visit(new BuildResource(project, "target.lua"));
    logger.debug("Build result: {}", buildDetails.getResult());
    assertTrue(buildDetails.getResult().contains("hello"));
    assertTrue(buildDetails.getResult().contains("world"));

  }

}