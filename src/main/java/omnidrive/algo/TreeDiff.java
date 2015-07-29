package omnidrive.algo;

import com.google.common.collect.Sets;

import java.util.*;

public class TreeDiff<L extends TreeNode, R extends TreeNode> {

    final private Comparator<L, R> comparator;

    public TreeDiff(Comparator<L, R> comparator) {
        this.comparator = comparator;
    }

    public DiffResult<L, R> run(L left, R right) {
        DiffResult<L, R> result = new DiffResult<>();
        run(left, right, result);
        return result;
    }

    private void run(L left, R right, DiffResult<L, R> result) {
        Map<String, L> leftChildren = childrenMap(left);
        Map<String, R> rightChildren = childrenMap(right);
        for (String name : Sets.union(leftChildren.keySet(), rightChildren.keySet())) {
            boolean leftContains = leftChildren.containsKey(name);
            boolean rightContains = rightChildren.containsKey(name);
            if (leftContains && rightContains) {
                L leftChild = leftChildren.get(name);
                R rightChild = rightChildren.get(name);
                if (!comparator.areEqual(leftChild, rightChild)) {
                    result.modified.add(new Pair<>(leftChild, rightChild));
                }
                run(leftChild, rightChild, result);
            } else if (leftContains) {
                result.addedLeft.add(leftChildren.get(name));
            } else if (rightContains) {
                result.addedRight.add(rightChildren.get(name));
            }
        }
    }

    private <T extends TreeNode<T>> Map<String, T> childrenMap(T tree) {
        Map<String, T> childrenMap = new HashMap<>();
        for (T child : tree.getChildren()) {
            childrenMap.put(child.getName(), child);
        }
        return childrenMap;
    }

    public static class DiffResult<L, R> {

        final private Set<L> addedLeft = new HashSet<>();

        final private Set<R> addedRight = new HashSet<>();

        final private Set<Pair<L, R>> modified = new HashSet<>();

        public boolean areEqual() {
            return addedLeft.isEmpty() &&
                    addedRight.isEmpty() &&
                    modified.isEmpty();
        }

        public Set<R> addedRight() {
            return addedRight;
        }

        public Set<L> addedLeft() {
            return addedLeft;
        }

        public Set<Pair<L, R>> modified() {
            return modified;
        }

    }
}
