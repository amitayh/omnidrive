package omnidrive.ui.general;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import omnidrive.api.auth.AuthListener;
import omnidrive.api.auth.Authorizer;
import omnidrive.api.base.BaseException;
import omnidrive.api.base.AccountType;

public class LoginView {

    private final Stage loginStage;

    public LoginView() {
        this.loginStage = new Stage();
    }

    public void show(final AuthListener authListener, final Authorizer authorizer, final AccountType type, final String authUrl) {
        final WebView browser = new WebView();
        final WebEngine engine = browser.getEngine();

        BorderPane authPane = new BorderPane();

        engine.load(authUrl);

        authPane.setCenter(browser);

        // listen to document load completed event
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.READY || newState == Worker.State.SUCCEEDED) {
                    try {
                        authorizer.fetchAuthCode(engine);
                    } catch (BaseException ex) {
                        authListener.failure(type, ex.getMessage());
                    }
                } else if (newState == Worker.State.FAILED) {
                    authListener.failure(type, engine.getLoadWorker().getException().getMessage());
                }
            }
        });

        // create scene
        this.loginStage.setTitle(type.toString());
        Scene scene = new Scene(authPane, 750, 500);
        this.loginStage.setScene(scene);
        this.loginStage.showAndWait();
    }

    public void close() {
        this.loginStage.close();
    }
}
