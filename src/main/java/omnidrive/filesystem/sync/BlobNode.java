package omnidrive.filesystem.sync;

import omnidrive.filesystem.manifest.entry.Blob;

import java.util.Collections;
import java.util.Map;

public class BlobNode extends EntryNode {

    final private Blob blob;

    public BlobNode(Blob blob) {
        this.blob = blob;
    }

    @Override
    public Map<String, EntryNode> getChildren() {
        return Collections.emptyMap();
    }

}
