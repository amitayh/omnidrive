package omnidrive.filesystem.sync;

import omnidrive.algo.Comparator;
import omnidrive.algo.Pair;
import omnidrive.algo.TreeDiff;
import omnidrive.api.base.AccountType;
import omnidrive.api.base.BaseAccount;
import omnidrive.api.managers.AccountsManager;
import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.entry.Blob;
import omnidrive.filesystem.manifest.entry.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Syncer {

    final private Path rootPath;

    final private AccountsManager accountsManager;

    public Syncer(Path rootPath, AccountsManager accountsManager) {
        this.rootPath = rootPath;
        this.accountsManager = accountsManager;
    }

    public void fullSync(Manifest manifest) throws Exception {
        Comparator<FileNode, EntryNode> comparator = new SyncComparator();
        TreeDiff<FileNode, EntryNode> diff = new TreeDiff<>(comparator);
        FileNode left = new FileNode(rootPath.toFile());
        EntryNode right2 = new EntryNode(manifest, Paths.get(""), manifest.getRoot());
        TreeDiff.Result<FileNode, EntryNode> result = diff.run(left, right2);
        syncDiffResult(result, manifest);
    }

    private void syncDiffResult(TreeDiff.Result<FileNode, EntryNode> result, Manifest manifest) throws Exception {
        long manifestUpdateTime = manifest.getUpdatedTime();
        for (FileNode fileNode : result.addedLeft()) {
            File file = fileNode.getFile();
            if (file.lastModified() > manifestUpdateTime) {
                upload(file);
            }
        }
        for (EntryNode entry : result.addedRight()) {
            download(entry);
        }
        for (Pair<FileNode, EntryNode> pair : result.modified()) {
            // TODO
        }
    }

    private void upload(File file) {
        System.out.println("Upload");
        System.out.println(file);
    }

    private void download(EntryNode entry) throws Exception {
        if (entry.getType() == Entry.Type.BLOB) {
            Blob blob = entry.as(Blob.class);
            AccountType accountType = blob.getAccount();
            BaseAccount account = accountsManager.getAccount(accountType);
            File file = rootPath.resolve(entry.getPath()).toFile();
            OutputStream outputStream = new FileOutputStream(file);
            account.downloadFile(blob.getId(), outputStream);
        }
    }

//    private void download(Path path, TreeItem item) throws Exception {
//        Path downloadPath = path.resolve(item.getName());
//        File file = downloadPath.toFile();
//        if (!file.exists() || file.lastModified() < item.getLastModified()) {
//            OutputStream outputStream = new FileOutputStream(file);
//            account.downloadFile(item.getId(), outputStream);
//        }
//    }

//    private boolean createDir(Path path) {
//        File dir = path.toFile();
//        return dir.mkdir();
//    }

    private class SyncComparator implements Comparator<FileNode, EntryNode> {

        @Override
        public boolean areEqual(FileNode left, EntryNode right) {
            return true;
        }

    }

}
