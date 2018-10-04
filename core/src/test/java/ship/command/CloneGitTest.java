package ship.command;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import ship.AbstractTestCase;
import ship.FileSet;

public class CloneGitTest extends AbstractTestCase {

  @Test
  @PrepareForTest({CloneGit.class, FetchResult.class, RevCommit.class})
  public void testExecute() throws Exception {
    final byte[] fileContent = randomUUID().toString().getBytes();
    final InMemoryRepository repository = mock(InMemoryRepository.class);
    final Git git = mock(Git.class);
    final RemoteAddCommand remoteAddCommand = mock(RemoteAddCommand.class);
    final FetchCommand fetchCommand = mock(FetchCommand.class);
    final FetchResult fetchResult = mock(FetchResult.class);
    final Ref headRef = mock(Ref.class);
    final ObjectId headId = mock(ObjectId.class);
    final RevWalk revWalk = mock(RevWalk.class);
    final RevCommit revCommit = mock(RevCommit.class);
    final RevTree revTree = mock(RevTree.class);
    final TreeWalk treeWalk = mock(TreeWalk.class);
    final ObjectId fileId = mock(ObjectId.class);
    final ObjectLoader objectLoader = mock(ObjectLoader.class);

    whenNew(InMemoryRepository.class).withAnyArguments().thenReturn(repository);
    whenNew(Git.class).withAnyArguments().thenReturn(git);
    when(git.remoteAdd()).thenReturn(remoteAddCommand);
    when(remoteAddCommand.setName(anyString())).thenReturn(remoteAddCommand);
    when(remoteAddCommand.setUri(any(URIish.class))).thenReturn(remoteAddCommand);
    when(git.fetch()).thenReturn(fetchCommand);
    when(fetchCommand.setRemote(anyString())).thenReturn(fetchCommand);
    when(fetchCommand.setRefSpecs(anyString())).thenReturn(fetchCommand);
    when(fetchCommand.call()).thenReturn(fetchResult);
    when(fetchResult.getAdvertisedRef(eq(HEAD))).thenReturn(headRef);
    when(headRef.getObjectId()).thenReturn(headId);
    whenNew(RevWalk.class).withAnyArguments().thenReturn(revWalk);
    when(revWalk.parseCommit(any(AnyObjectId.class))).thenReturn(revCommit);
    when(revCommit.getTree()).thenReturn(revTree);
    whenNew(TreeWalk.class).withAnyArguments().thenReturn(treeWalk);
    final AtomicInteger visitCounter = new AtomicInteger(0);
    when(treeWalk.next()).then(invocation -> 0 == visitCounter.getAndIncrement());
    when(treeWalk.getObjectId(0)).thenReturn(fileId);
    when(treeWalk.getPathString()).thenReturn(randomUUID().toString());
    when(repository.open(fileId)).thenReturn(objectLoader);
    doAnswer(invocation -> {
      try {
        ((OutputStream) invocation.getArgument(0)).write(fileContent);
      } catch (final IOException ex) {
        fail("Unexpected exceptioni");
      }
      return null;
    }).when(objectLoader).copyTo(any(OutputStream.class));

    final String packageName = randomUUID().toString();
    final CloneGit cloneGit = new CloneGit();
    cloneGit.setArguments(asList(packageName));
    cloneGit.execute();

    final FileSet fileSet = cloneGit.getFileSet();
    assertFalse(fileSet.getFiles().isEmpty());
  }

}