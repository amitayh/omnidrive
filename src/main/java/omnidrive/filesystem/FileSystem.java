package omnidrive.filesystem;

import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.MapDbManifest;
import omnidrive.util.MapDbUtils;
import org.mapdb.DB;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystem {

    public static final String USER_HOME = System.getProperty("user.home");

    public static final String MANIFEST_FILENAME = ".manifest";

    public static final String ROOT_NAME = "OmniDrive";

    private final Path root;

    public FileSystem(Path root) {
        this.root = root;
    }

    public Path getRootPath() {
        return root;
    }

    public boolean manifestExists() {
        File manifest = getManifestFile();
        return manifest.isFile();
    }

    public Manifest getManifest() {
        File manifest = getManifestFile();
        DB db = MapDbUtils.createFileDb(manifest);
        return new MapDbManifest(db);
    }

    public File getManifestFile() {
        return root.resolve(MANIFEST_FILENAME).toFile();
    }

    public static Path defaultRootPath() {
        return Paths.get(USER_HOME, ROOT_NAME);
    }

}
