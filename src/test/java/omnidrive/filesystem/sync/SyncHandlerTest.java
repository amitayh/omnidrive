package omnidrive.filesystem.sync;

import com.google.common.io.CharStreams;
import omnidrive.api.base.BaseAccount;
import omnidrive.api.managers.AccountsManager;
import omnidrive.filesystem.BaseTest;
import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.MapDbManifest;
import omnidrive.filesystem.manifest.entry.Blob;
import omnidrive.filesystem.manifest.entry.Tree;
import omnidrive.filesystem.manifest.entry.TreeItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SyncHandlerTest extends BaseTest {

    public static final String UPLOAD_ID = "new-id";

    private Manifest manifest = createInMemoryManifest();

    private BaseAccount account = mock(BaseAccount.class);

    private UploadStrategy uploadStrategy = mock(UploadStrategy.class);

    private AccountsManager accountsManager = mock(AccountsManager.class);

    private SyncHandler handler = new SyncHandler(manifest, uploadStrategy, accountsManager);

    @Before
    public void setUp() throws Exception {
        when(uploadStrategy.selectAccount()).thenReturn(account);
        when(accountsManager.getActiveAccounts()).thenReturn(Collections.singletonList(account));
        when(account.uploadFile(anyString(), any(InputStream.class), anyLong())).thenReturn(UPLOAD_ID);
    }

    @Test
    public void testCreateFileUploadsToAccountUsingStrategy() throws Exception {
        File file = getResource("hello.txt");
        ArgumentCaptor<String> nameArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> inputStreamArgument = ArgumentCaptor.forClass(InputStream.class);

        handler.create(file);

        verify(account).uploadFile(nameArgument.capture(), inputStreamArgument.capture(), eq(file.length()));
        assertValidUUID(nameArgument.getValue());
        assertEquals("Hello World", toString(inputStreamArgument.getValue()));
    }

    @Test
    public void testCreateFileAddsToManifest() throws Exception {
        File file = getResource("hello.txt");

        String id = handler.create(file);

        assertEquals(UPLOAD_ID, id);
        Blob expected = new Blob(id, file.length(), account.getName());
        assertEquals(expected, manifest.getBlob(id));
    }

    @Test
    public void testCreateBlobSyncsManifestToAllAccounts() throws Exception {
        File file = getResource("hello.txt");

        handler.create(file);

        // TODO: who syncs manifest? (handler / manifest / other)
//        verify(manifest).sync(account);
    }

    @Test
    public void testCreateBlobAddsEntryInParentTree() throws Exception {
        manifest = createInMemoryManifest();
        handler = new SyncHandler(manifest, uploadStrategy, accountsManager);

        Tree root = manifest.getRoot();
        assertTrue(root.getItems().isEmpty());

        String name = "hello.txt";
        File file = getResource(name);
        handler.create(file);

        List<TreeItem> items = manifest.getRoot().getItems();
        assertEquals(1, items.size());
        assertEquals(name, items.get(0).getName());
    }

    @Test
    public void testCreateEmptyDirAddsToManifest() throws Exception {
        File dir = Files.createTempDirectory("empty").toFile();

        String id = handler.create(dir);

        assertValidUUID(id);
        Tree expected = new Tree(id);
        assertEquals(expected, manifest.getTree(id));

        //noinspection ResultOfMethodCallIgnored
        dir.delete();
    }

    private void assertValidUUID(String id) {
        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(id);
        } catch (Exception e) {
            fail("Invalid UUID: " + id);
        }
    }

    private String toString(InputStream value) throws IOException {
        return CharStreams.toString(new InputStreamReader(value));
    }

    private Manifest createInMemoryManifest() {
        DB db = DBMaker.newMemoryDB().make();
        return new MapDbManifest(db);
    }

}