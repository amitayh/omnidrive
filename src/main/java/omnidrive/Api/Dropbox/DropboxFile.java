package omnidrive.api.Dropbox;

import com.dropbox.core.DbxEntry;
import omnidrive.api.base.*;

import java.util.Date;

public class DropboxFile implements BaseFile {

    private DropboxUser owner;
    private DbxEntry entry;

    public DropboxFile(DbxEntry entry, DropboxUser owner) throws DropboxException {
        this.owner = owner;

        if (!entry.isFile()) {
            throw new DropboxException("Not a file.");
        } else {
            this.entry = entry;
        }
    }

    public BaseUser getOwner() {
        return this.owner;
    }

    public String getPath() {
        return this.entry.asFile().path;
    }

    public String getName() {
        return this.entry.asFile().name;
    }

    public Date getLastModified() {
        return this.entry.asFile().lastModified;
    }

    public long getSize() {
        return this.entry.asFile().numBytes;
    }
}
