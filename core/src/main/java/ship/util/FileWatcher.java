/*
 * @copyright defined in LICENSE.txt
 */

package ship.util;

import static hera.util.ArrayUtils.isEmpty;
import static hera.util.ThreadUtils.trySleep;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import hera.server.ServerEvent;
import hera.server.ThreadServer;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Monitor file changes.
 */
public class FileWatcher extends ThreadServer implements Runnable {

  /**
   * Reset.
   */
  public static final int RESET = 0x11;

  /**
   * File add event type.
   */
  public static final int FILE_ADDED = 0x12;

  /**
   * File remove event type.
   */
  public static final int FILE_REMOVED = 0x13;

  /**
   * File modification event type.
   */
  public static final int FILE_CHANGED = 0x14;

  /**
   * Any change event type.
   */
  public static final int ANY_CHANGED = 0x18;

  /**
   * Interval to check.
   */
  @Getter
  @Setter
  protected long interval = 300;

  /**
   * File's last changed time.
   */
  protected final HashMap<File, Long> base2lastModified1 = new HashMap<>();

  protected final File base;

  protected long lastModified = 0;

  protected Set<File> previouslyChecked = new HashSet<>();

  protected Set<String> ignores = new HashSet<>();

  /**
   * Constructor with watch service and base path.
   *
   * @param basePath path to monitor
   */
  public FileWatcher(final File basePath) {
    this.base = basePath;
  }

  public void addIgnore(final String name) {
    ignores.add(name);
  }

  /**
   * Rake file changes.
   *
   * <p>
   *   Save files to be checked into {@code checked} and files to be changed into {@code changed}.
   * </p>
   *
   * @param checked container to save checked files
   * @param changed container to save changed files
   */
  protected void rake(final Set<File> checked, final Set<File> changed) {
    logger.trace("Base: {}", base);
    logger.trace("Base's last modified: {}", lastModified);

    long max = 0;
    final Queue<File> files = new LinkedList<>(asList(base));
    while (!files.isEmpty()) {
      final File file = files.remove();
      logger.trace("File: {}", file);
      final long lastModifiedInFile = file.lastModified();
      max = Math.max(lastModifiedInFile, max);

      if (lastModified < lastModifiedInFile) {
        changed.add(file);
      }
      checked.add(file);
      final File[] children = file.listFiles();

      if (!isEmpty(children)) {
        logger.trace("Find {} files", children.length);
        files.addAll(stream(children)
            .filter(child -> !ignores.contains(child.getName())).collect(toList()));
      }
    }

    lastModified = max;
  }

  @Override
  @SuppressWarnings({"unchecked", "unsafe"})
  protected void process() throws Exception {
    trySleep(getInterval());
    final HashSet<File> checkedFiles = new HashSet<>();
    final HashSet<File> changed = new HashSet<>();

    rake(checkedFiles, changed);
    logger.debug("Changed: {}", changed);

    final BeforeAndAfter<File> beforeAndAfter =
        new BeforeAndAfter<>(previouslyChecked, checkedFiles);
    previouslyChecked = checkedFiles;

    final Set<File> added = beforeAndAfter.getAddedItems();
    final Set<File> removed = beforeAndAfter.getRemovedItems();
    final Set<File> any = asList(added, removed, changed).stream()
        .flatMap(Collection::stream).collect(toSet());

    if (!added.isEmpty()) {
      logger.info("{} added", added);
      fireEvent(new ServerEvent(this, FILE_ADDED, unmodifiableCollection(added)));
    }
    if (!removed.isEmpty()) {
      logger.info("{} removed", removed);
      fireEvent(new ServerEvent(this, FILE_REMOVED, unmodifiableCollection(removed)));
    }
    if (!changed.isEmpty()) {
      logger.info("{} changed", changed);
      fireEvent(new ServerEvent(this, FILE_CHANGED, unmodifiableCollection(changed)));
    }
    if (!any.isEmpty()) {
      logger.info("{} detected", changed);
      fireEvent(new ServerEvent(this, ANY_CHANGED, unmodifiableCollection(any)));
    }
  }
}
