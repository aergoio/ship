package ship.build;

import static java.util.UUID.randomUUID;

import org.junit.Test;
import ship.ProjectFile;
import ship.build.res.Project;
import ship.exception.CyclicDependencyException;

public class CallStackTest {
  protected final ProjectFile projectFile = new ProjectFile();
  protected final Project project = new Project(randomUUID().toString(), projectFile);
  protected final Resource resource1 = new Resource(project, randomUUID().toString());
  protected final Resource resource2 = new Resource(project, randomUUID().toString());

  @Test(expected = CyclicDependencyException.class)
  public void testEnter() {
    final CallStack callStack = new CallStack();
    callStack.enter(resource1);
    callStack.enter(resource2);
    callStack.enter(resource2);
  }

  @Test
  public void testExit() {
    final CallStack callStack = new CallStack();
    callStack.enter(resource1);
    callStack.enter(resource2);
    callStack.exit(resource2);
    callStack.exit(resource1);
  }

}