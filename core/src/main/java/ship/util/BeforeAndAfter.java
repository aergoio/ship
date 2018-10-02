package ship.util;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BeforeAndAfter<T> {

  protected final Set<T> before;

  protected final Set<T> after;

  /**
   * Get new added items in {@link #after}.
   *
   * @return added item set
   */
  public Set<T> getAddedItems() {
    final HashSet<T> added = new HashSet<>(after);
    added.removeAll(before);
    return added;
  }

  /**
   * Get removed items in {@link #after}.
   *
   * @return removed item set
   */
  public Set<T> getRemovedItems() {
    final HashSet<T> removed = new HashSet<>(before);
    removed.removeAll(after);
    return removed;
  }

  /**
   * Get existent items in both {@link #before} and {@link #after}.
   *
   * @return commonly existent item set
   */
  public Set<T> getIntersectedItems() {
    final HashSet<T> intersection = new HashSet<>(before);
    intersection.retainAll(after);
    return intersection;
  }

  /**
   * Get items in either {@link #before} or {@link #after}.
   *
   * @return union set
   */
  public Set<T> getUnionedItems() {
    final HashSet<T> union = new HashSet<>(before);
    union.addAll(after);
    return union;
  }




}
