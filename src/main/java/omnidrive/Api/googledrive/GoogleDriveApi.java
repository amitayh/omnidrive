package omnidrive.Api.googledrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import javafx.scene.web.WebEngine;
import omnidrive.Api.Base.BaseApi;
import omnidrive.Api.Base.BaseException;
import omnidrive.Api.managers.LoginManager;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleDriveApi extends BaseApi {

    private static final String APP_NAME = "Google Drive";
    private static final String CLIENT_ID = "438388195219-sf38d0f4bbj4t9at3e9n72uup3cfsb8m.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "57T8iW2bKRFZJSiX69Dr4cQV";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private LoginManager loginManager;

    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = new JacksonFactory();

    private GoogleAuthorizationCodeFlow auth;

    public GoogleDriveApi() {
        super(APP_NAME, CLIENT_ID, CLIENT_SECRET);

        this.auth = new GoogleAuthorizationCodeFlow.Builder(
                this.httpTransport, this.jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto").build();
    }

    /*****************************************************************
     * Interface methods
     *****************************************************************/

    public final void login(LoginManager loginManager) throws BaseException {
        addListener(loginManager);

        this.loginManager = loginManager;

        String authUrl = this.auth.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();

        openAuthUrl(authUrl);
    }

    public final void fetchAccessToken(WebEngine engine) throws BaseException {
        String code = null;

        String title = engine.getTitle();
        if (title != null) {
            if (title.contains("Success code")) {
                try {
                    code = title.split("=")[1].trim();
                } catch (Exception ex) {
                    code = null;
                }
            }
        }

        if (code != null) {
            try {
                GoogleTokenResponse response = this.auth.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
                GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

                //Create a new authorized API client
                Drive service = new Drive.Builder(httpTransport, jsonFactory, credential).build();

                notifyLoginListeners(service);
            } catch (IOException ex) {
                throw new GoogleDriveException("Failed to finish auth process.");
            }
        }
    }
}
