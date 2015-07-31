package omnidrive.algo;

import java.util.Map;

public interface TreeNode<T extends TreeNode> {
    Map<String, T> getChildren();
}
