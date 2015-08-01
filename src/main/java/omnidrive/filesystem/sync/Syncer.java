package omnidrive.filesystem.sync;

import omnidrive.algo.Comparator;
import omnidrive.algo.TreeDiff;
import omnidrive.api.base.BaseAccount;
import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.entry.TreeItem;
import omnidrive.filesystem.manifest.walker.ItemVisitor;
import omnidrive.filesystem.manifest.walker.ManifestWalker;
import omnidrive.filesystem.manifest.walker.SimpleVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class Syncer {

    final private Path rootPath;

    final private BaseAccount account;

    public Syncer(Path rootPath, BaseAccount account) {
        this.rootPath = rootPath;
        this.account = account;
    }

    public void fullSync(Manifest manifest) throws Exception {
        Comparator<FileNode, EntryNode> comparator = new SyncComparator();
        TreeDiff<FileNode, EntryNode> diff = new TreeDiff<>(comparator);
        FileNode left = new FileNode(rootPath.toFile());
        TreeNode right = new TreeNode(manifest, manifest.getRoot());
        TreeDiff.Result<FileNode, EntryNode> result = diff.run(left, right);
        syncDiffResult(result);


        ManifestWalker walker = new ManifestWalker(manifest);
        ItemVisitor visitor = new SyncVisitor(rootPath);
        walker.walk(visitor);
    }

    private void syncDiffResult(TreeDiff.Result<FileNode, EntryNode> result) {

    }

    private void download(Path path, TreeItem item) throws Exception {
        Path downloadPath = path.resolve(item.getName());
        File file = downloadPath.toFile();
        if (!file.exists() || file.lastModified() < item.getLastModified()) {
            OutputStream outputStream = new FileOutputStream(file);
            account.downloadFile(item.getId(), outputStream);
        }
    }

    private boolean createDir(Path path) {
        File dir = path.toFile();
        return dir.mkdir();
    }

    private class SyncComparator implements Comparator<FileNode, EntryNode> {

        @Override
        public boolean areEqual(FileNode left, EntryNode right) {
            return true;
        }

    }

    private class SyncVisitor extends SimpleVisitor {

        private Path path;

        public SyncVisitor(Path path) {
            this.path = path;
        }

        public void preVisit(TreeItem item) throws Exception {
            path = path.resolve(item.getName());
            createDir(path);
        }

        public void visit(TreeItem item) throws Exception {
            download(path, item);
        }

        public void postVisit(TreeItem item) throws Exception {
            path = path.getParent();
        }

    }

    private class OtherVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            System.out.println(file);
            return FileVisitResult.CONTINUE;
        }

    }

    private class SyncActions {
        Set<String> delete = new HashSet<>();
        Set<String> upload = new HashSet<>();
        Set<String> download = new HashSet<>();
    }

}
