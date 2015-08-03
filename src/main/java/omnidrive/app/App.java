package omnidrive.app;

import omnidrive.api.base.BaseAccount;
import omnidrive.api.managers.AccountsManager;
import omnidrive.filesystem.FileSystem;
import omnidrive.filesystem.manifest.Manifest;
import omnidrive.filesystem.manifest.ManifestSync;
import omnidrive.filesystem.manifest.MapDbManifest;
import omnidrive.filesystem.manifest.MapDbManifestSync;
import omnidrive.filesystem.sync.SimpleUploadStrategy;
import omnidrive.filesystem.sync.SyncHandler;
import omnidrive.filesystem.sync.UploadStrategy;
import omnidrive.filesystem.watcher.Handler;
import omnidrive.filesystem.watcher.Watcher;
import omnidrive.ui.managers.UIManager;
import omnidrive.util.MapDbUtils;
import org.mapdb.DB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.List;

public class App {

    final private FileSystem fileSystem;

    final private AccountsManager accountsManager;

    final private UIManager uiManager;

    public App(FileSystem fileSystem, AccountsManager accountsManager, UIManager uiManager) {
        this.fileSystem = fileSystem;
        this.accountsManager = accountsManager;
        this.uiManager = uiManager;
    }

    public void start() throws Exception {
        if (isFirstRun()) {
            startFirstRun();
        } else {
            startSubsequentRun();
        }
    }

    private void startFirstRun() throws Exception {
        initFileSystem();
        startWatcherThread();
        openAccountsSelector();
        // TODO
    }

    private void startSubsequentRun() throws Exception {
        List<BaseAccount> registeredAccounts = getRegisteredAccounts();
        BaseAccount lruAccount = resolveLeastRecentlyUpdatedAccount(registeredAccounts);
        // TODO rewrite this
        fullSync(lruAccount);
        for (BaseAccount account : registeredAccounts) {
            if (account != lruAccount) {
                upstreamSync(account);
            }
        }
        startWatcherThread();
    }

    private void initFileSystem() {
    }

    private void openAccountsSelector() {
        uiManager.startGuiInFront(fileSystem.getRootPath());
    }

    private boolean isFirstRun() {
        return true;
        //return !fileSystem.manifestExists();
    }

    private List<BaseAccount> getRegisteredAccounts() throws Exception {
        Manifest manifest = fileSystem.getManifest();
        accountsManager.restoreAccounts(manifest.getAccountsMetadata());
        return accountsManager.getActiveAccounts();
    }

    private BaseAccount resolveLeastRecentlyUpdatedAccount(List<BaseAccount> accounts) throws Exception {
        long lruTime = 0;
        BaseAccount lruAccount = null;
        for (BaseAccount account : accounts) {
            long accountUpdateTime = getAccountUpdateTime(account);
            if (accountUpdateTime > lruTime) {
                lruTime = accountUpdateTime;
                lruAccount = account;
            }
        }
        return lruAccount;
    }

    private long getAccountUpdateTime(BaseAccount account) throws Exception {
        File tempFile = File.createTempFile("manifest", "db");
        OutputStream outputStream = new FileOutputStream(tempFile);
        account.downloadManifestFile(outputStream);
        outputStream.close();
        DB db = MapDbUtils.createFileDb(tempFile);
        Manifest manifest = new MapDbManifest(db);
        long updateTime = manifest.getUpdatedTime();
        db.close();
        assert tempFile.delete();
        return updateTime;
    }

    private void fullSync(BaseAccount account) {
//        Syncer syncer = new Syncer(FileSystem.getRootPath(), account);
//        syncer.fullSync();
    }

    private void upstreamSync(BaseAccount account) {

    }

    private void startWatcherThread() throws Exception {
        Path root = fileSystem.getRootPath();
        File manifestFile = fileSystem.getManifestFile();
        DB db = MapDbUtils.createFileDb(manifestFile);
        Manifest manifest = new MapDbManifest(db);
        ManifestSync manifestSync = new MapDbManifestSync(accountsManager, manifestFile, db);
        UploadStrategy uploadStrategy = new SimpleUploadStrategy(accountsManager);
        Handler handler = new SyncHandler(root, manifest, manifestSync, uploadStrategy, accountsManager);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Watcher watcher = new Watcher(watchService, handler);
        watcher.registerRecursive(root);

        Thread thread = new Thread(watcher);
        thread.setDaemon(true);
        thread.start();
    }

}
