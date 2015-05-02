package omnidrive.api.managers;

import omnidrive.api.base.AuthListener;
import omnidrive.api.base.BaseApi;
import omnidrive.api.base.DriveType;
import omnidrive.api.box.BoxApi;
import omnidrive.api.dropbox.DropboxApi;
import omnidrive.api.google.GoogleDriveApi;

public class ApiManager {

    private final BaseApi[] apis = new BaseApi[DriveType.values().length];

    public ApiManager() {
        for (DriveType type : DriveType.values()) {
            apis[type.ordinal()] = createApi(type);
        }
    }

    private BaseApi createApi(DriveType type) {
        BaseApi api = null;

        switch (type) {
            case Dropbox:
                api = new DropboxApi();
                break;
            case GoogleDrive:
                api = new GoogleDriveApi();
                break;
            case Box:
                api = new BoxApi();
                break;
        }

        return api;
    }

    public String login(DriveType type, AuthListener listener) throws Exception {
        String authUrl = null;

        if (this.apis[type.ordinal()] != null) {
            authUrl = this.apis[type.ordinal()].login(listener);
        }

        return authUrl;
    }

    public BaseApi getApi(DriveType type) {
        return this.apis[type.ordinal()];
    }
}