package omnidrive.filesystem.sync;

import com.google.inject.Inject;
import omnidrive.api.base.BaseAccount;
import omnidrive.api.managers.AccountsManager;
import omnidrive.filesystem.exception.InvalidFileException;
import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.ManifestSync;
import omnidrive.filesystem.manifest.entry.Blob;
import omnidrive.filesystem.manifest.entry.Entry;
import omnidrive.filesystem.manifest.entry.Tree;
import omnidrive.filesystem.manifest.entry.TreeItem;
import omnidrive.filesystem.watcher.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.UUID;

public class SyncHandler implements Handler {

    private final Path root;

    private final Manifest manifest;

    private final ManifestSync manifestSync;

    private final UploadStrategy uploadStrategy;

    private final AccountsManager accountsManager;

    @Inject
    public SyncHandler(Path root,
                       Manifest manifest,
                       ManifestSync manifestSync,
                       UploadStrategy uploadStrategy,
                       AccountsManager accountsManager) {
        this.root = root;
        this.manifest = manifest;
        this.manifestSync = manifestSync;
        this.uploadStrategy = uploadStrategy;
        this.accountsManager = accountsManager;
    }

    public String create(File file) throws Exception {
        String id;
        if (file.isFile()) {
            id = createFile(file);
        } else if (file.isDirectory()) {
            id = createDir(file);
        } else {
            throw new InvalidFileException();
        }
        manifestSync.upload();
        return id;
    }

    public String modify(File file) throws Exception {
        if (!file.isFile()) {
            throw new InvalidFileException();
        }
        Blob blob = getBlob(file);
        String id = blob.getId();
        Blob updated = new Blob(id, file.length(), blob.getAccount());
        BaseAccount account = getAccount(blob);
        account.removeFile(id);
        account.uploadFile(id, new FileInputStream(file), updated.getSize());
        manifest.put(updated);
        manifestSync.upload();
        return id;
    }

    public void delete(File file) throws Exception {
        Tree parent = findParent(file.toPath());
        TreeItem item = parent.getItem(file.getName());
        String id = item.getId();
        Blob blob = manifest.getBlob(id);
        BaseAccount account = getAccount(blob);
        account.removeFile(id);
        parent.removeItem(id);
        manifest.put(parent);
        manifest.remove(id);
        manifestSync.upload();
    }

    private String createFile(File file) throws Exception {
        long size = file.length();
        BaseAccount account = uploadStrategy.selectAccount();
        String id = account.uploadFile(randomId(), new FileInputStream(file), size);
        manifest.put(new Blob(id, size, accountsManager.toType(account)));
        addEntryToParent(file, Entry.Type.BLOB, id);
        return id;
    }

    private String createDir(File file) {
        String id = randomId();
        manifest.put(new Tree(id));
        addEntryToParent(file, Entry.Type.TREE, id);
        return id;
    }

    private String randomId() {
        return UUID.randomUUID().toString();
    }

    private void addEntryToParent(File file, Entry.Type type, String id) {
        Tree parent = findParent(file.toPath());
        parent.addItem(new TreeItem(type, id, file.getName()));
        manifest.put(parent);
    }

    private Blob getBlob(File file) {
        Tree parent = findParent(file.toPath());
        TreeItem item = parent.getItem(file.getName());
        return manifest.getBlob(item.getId());
    }

    private Tree findParent(Path file) {
        Tree current = manifest.getRoot();
        Path relative = root.relativize(file).getParent();
        if (relative != null) {
            for (Path part : relative) {
                TreeItem item = current.getItem(part.toString());
                current = manifest.getTree(item.getId());
            }
        }
        return current;
    }

    private BaseAccount getAccount(Blob blob) {
        return accountsManager.getAccount(blob.getAccount());
    }

}
