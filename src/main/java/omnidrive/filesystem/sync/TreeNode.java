package omnidrive.filesystem.sync;

import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.entry.Blob;
import omnidrive.filesystem.manifest.entry.Tree;
import omnidrive.filesystem.manifest.entry.TreeItem;

import java.util.HashMap;
import java.util.Map;

public class TreeNode extends EntryNode {

    final private Manifest manifest;

    final private Tree tree;

    public TreeNode(Manifest manifest, Tree tree) {
        this.manifest = manifest;
        this.tree = tree;
    }

    @Override
    public Map<String, EntryNode> getChildren() {
        Map<String, EntryNode> children = new HashMap<>();
        for (TreeItem item : tree.getItems()) {
            EntryNode entry = null;
            String id = item.getId();
            switch (item.getType()) {
                case BLOB: entry = new BlobNode(manifest.get(id, Blob.class)); break;
                case TREE: entry = new TreeNode(manifest, manifest.get(id, Tree.class)); break;
            }
            children.put(item.getName(), entry);
        }
        return children;
    }

}
