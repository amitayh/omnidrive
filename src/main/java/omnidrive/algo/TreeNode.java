package omnidrive.algo;

public interface TreeNode<T extends TreeNode> {
    String getName();
    Iterable<T> getChildren();
}
