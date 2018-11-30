/*
 * @copyright defined in LICENSE.txt
 */

package ship.command;

import static org.eclipse.jgit.lib.Constants.HEAD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import ship.FileContent;
import ship.FileSet;

public class CloneGit extends AbstractCommand {

  protected static final String URI_PATTERN = "https://github.com/{0}.git";

  @Getter
  @Setter
  protected FileSet fileSet = new FileSet();

  @Override
  public void execute() throws Exception {
    final String packageName = getArgument(0);
    final String branch = getOptionalArgument(1).orElse("master");
    logger.debug("Package name: {}, Branch: {}", packageName, branch);

    final String remoteUri = MessageFormat.format(URI_PATTERN, packageName);
    final InMemoryRepository repository = new InMemoryRepository(new DfsRepositoryDescription());
    final Git git = new Git(repository);
    git.remoteAdd().setName("origin").setUri(new URIish(remoteUri)).call();

    logger.trace("Git: {}", git);

    final FetchResult fetchResult = git.fetch()
        .setRemote("origin")
        .setRefSpecs("+refs/heads/" + branch + ":refs/remotes/origin/" + branch)
        .call();
    logger.debug("Fetch result: {}", fetchResult);

    logger.debug("Refs: {}", fetchResult.getAdvertisedRefs());
    final Ref fetchHead = fetchResult.getAdvertisedRef(HEAD);

    final ObjectId commitId = fetchHead.getObjectId();
    logger.debug("Commit id: {}", commitId);

    try (final RevWalk revWalk = new RevWalk(repository)) {
      visit(repository, revWalk, commitId);
    }
    logger.debug("FileSet: {}", fileSet);
  }

  protected void visit(final Repository repository, final RevWalk revWalk,
      final ObjectId commitId) throws IOException {
    final RevCommit revCommit = revWalk.parseCommit(commitId);
    final RevTree revTree = revCommit.getTree();
    logger.debug("Having tree: " + revTree);

    // now try to find a specific file
    try (final TreeWalk treeWalk = new TreeWalk(repository)) {
      treeWalk.addTree(revTree);
      treeWalk.setRecursive(true);
      while (treeWalk.next()) {
        visit(repository, treeWalk);
      }
    }
    revWalk.dispose();
  }

  protected void visit(final Repository repository, final TreeWalk treeWalk) throws IOException {
    final String path = treeWalk.getPathString();
    logger.debug("Path: {}", path);
    logger.trace("Subtree: {}", treeWalk.isSubtree());
    if (treeWalk.isSubtree()) {
      treeWalk.enterSubtree();
    } else {
      final ObjectId objectId = treeWalk.getObjectId(0);
      final FileContent file = new FileContent(path, () -> {
        try {
          final ObjectLoader loader = repository.open(objectId);
          final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          loader.copyTo(byteOut);
          return new ByteArrayInputStream(byteOut.toByteArray());
        } catch (final IOException e) {
          throw new IllegalStateException(e);
        }
      });
      fileSet.add(file);
      logger.debug("{} added", file);
    }
  }

  public Stream<FileContent> stream() {
    return fileSet.stream();
  }
}
