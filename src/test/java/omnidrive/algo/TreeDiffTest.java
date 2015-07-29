package omnidrive.algo;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TreeDiffTest {

    private SimpleTreeComparator comparator = new SimpleTreeComparator();

    private TreeDiff<SimpleTree, SimpleTree> diff = new TreeDiff<>(comparator);

    @Test
    public void testTwoEmptyTreesAreEqual() throws Exception {
        // Given empty trees
        String name = "root";
        SimpleTree left = new SimpleTree(name);
        SimpleTree right = new SimpleTree(name);

        // When you diff them
        TreeDiff.DiffResult result = diff.run(left, right);

        // Then the result is equal
        assertTrue(result.areEqual());
    }

    @Test
    public void testTwoTreesWithOneItemAreEqual() throws Exception {
        // Given trees with one child with same name
        String name = "root";
        String childName = "child";
        SimpleTree left = new SimpleTree(name, new SimpleTree(childName));
        SimpleTree right = new SimpleTree(name, new SimpleTree(childName));

        // When you diff them
        TreeDiff.DiffResult result = diff.run(left, right);

        // Then the result is equal
        assertTrue(result.areEqual());
    }

    @Test
    public void testLeftTreeIsEmptyAndRightTreeHasAChildNotEqual() throws Exception {
        String name = "root";

        // Given the left tree is empty
        SimpleTree left = new SimpleTree(name);

        // And the right tree has one child
        String childName = "child";
        SimpleTree right = new SimpleTree(name, new SimpleTree(childName));

        // When you diff them
        TreeDiff.DiffResult result = diff.run(left, right);

        // Then the result is not equal
        assertFalse(result.areEqual());
    }

    @Test
    public void testLeftTreeIsEmptyAndRightTreeHasAChildAdded() throws Exception {
        String name = "root";

        // Given the left tree is empty
        SimpleTree left = new SimpleTree(name);

        // And the right tree has one child
        String childName = "child";
        SimpleTree child = new SimpleTree(childName);
        SimpleTree right = new SimpleTree(name, child);

        // When you diff them
        TreeDiff.DiffResult<SimpleTree, SimpleTree> result = diff.run(left, right);

        // Then the result contains the left child
        Set<SimpleTree> addedRight = result.addedRight();
        assertEquals(1, addedRight.size());
        assertTrue(addedRight.contains(child));
    }

    @Test
    public void testRightTreeIsEmptyAndLeftTreeHasAChildAdded() throws Exception {
        String name = "root";

        // Given the left tree has one child
        String childName = "child";
        SimpleTree child = new SimpleTree(childName);
        SimpleTree left = new SimpleTree(name, child);

        // And the right tree is empty
        SimpleTree right = new SimpleTree(name);

        // When you diff them
        TreeDiff.DiffResult<SimpleTree, SimpleTree> result = diff.run(left, right);

        // Then you result contains the right child
        Set<SimpleTree> addedLeft = result.addedLeft();
        assertEquals(1, addedLeft.size());
        assertTrue(addedLeft.contains(child));
    }

    @Test
    public void testUseAComparatorToCompareNodesWithSameName() throws Exception {
        String name = "root";
        String childName = "child";

        // Given the left tree has a child
        SimpleTree leftChild = new SimpleTree(childName, 1);
        SimpleTree left = new SimpleTree(name, leftChild);

        // And the right tree has a different child with same name
        SimpleTree rightChild = new SimpleTree(childName, 2);
        SimpleTree right = new SimpleTree(name, rightChild);

        // When you diff them
        TreeDiff.DiffResult<SimpleTree, SimpleTree> result = diff.run(left, right);

        // Then get the different nodes in the result
        Set<Pair<SimpleTree, SimpleTree>> modified = result.modified();
        assertEquals(1, modified.size());
        Pair<SimpleTree, SimpleTree> pair = modified.iterator().next();
        assertEquals(leftChild, pair.getLeft());
        assertEquals(rightChild, pair.getRight());
    }

    @Test
    public void testRecursiveComparison() throws Exception {
        String name = "root";
        String childName = "child";
        String grandchildName = "grandchild";

        // Given two trees with child with same name
        SimpleTree leftGrandchild = new SimpleTree(grandchildName, 1);
        SimpleTree leftChild = new SimpleTree(childName, leftGrandchild);
        SimpleTree left = new SimpleTree(name, leftChild);

        SimpleTree rightGrandchild = new SimpleTree(grandchildName, 2);
        SimpleTree rightChild = new SimpleTree(childName, rightGrandchild);
        SimpleTree right = new SimpleTree(name, rightChild);

        // When you diff them
        TreeDiff.DiffResult<SimpleTree, SimpleTree> result = diff.run(left, right);

        // Then you see the changes in the subtrees
        Set<Pair<SimpleTree, SimpleTree>> modified = result.modified();
        Pair<SimpleTree, SimpleTree> pair = modified.iterator().next();
        assertEquals(leftGrandchild, pair.getLeft());
        assertEquals(rightGrandchild, pair.getRight());
    }

    private class SimpleTree implements TreeNode<SimpleTree> {

        public final String name;

        public final int value;

        private final List<SimpleTree> children;

        private SimpleTree(String name, SimpleTree... children) {
            this(name, 0, children);
        }

        private SimpleTree(String name, int value, SimpleTree... children) {
            this.name = name;
            this.value = value;
            this.children = Arrays.asList(children);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Iterable<SimpleTree> getChildren() {
            return children;
        }

    }

    private class SimpleTreeComparator implements Comparator<SimpleTree, SimpleTree> {
        @Override
        public boolean areEqual(SimpleTree left, SimpleTree right) {
            return left.value == right.value;
        }
    }
}