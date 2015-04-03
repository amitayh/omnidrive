package omnidrive.api.base;

import omnidrive.api.managers.LoginManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseApi implements Authorizer {

    protected LoginManager loginManager;

    protected final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    private final String appName;
    private final String appId;
    private final String appSecret;

    public BaseApi(String appName, String appId, String appSecret) {
        this.appName = appName;
        this.appId = appId;
        this.appSecret = appSecret;
    }

    public void login(LoginManager loginManager) throws BaseException {
        addListener(loginManager);

        this.loginManager = loginManager;

        String authUrl = authorize();

        if (authUrl != null) {
            this.loginManager.showLoginView(this, authUrl);
        } else {
            throw new BaseException("Auth url is empty.");
        }
    }

    public String getName() {
        return this.appName;
    }

    protected String getAppId() {
        return this.appId;
    }

    protected String getAppSecret() {
        return this.appSecret;
    }

    protected void notifyLoginListeners(Object property) {
        for (PropertyChangeListener listener : this.listeners) {
            listener.propertyChange(new PropertyChangeEvent(this, "login_succeed", null, property));
        }
    }

    protected void addListener(PropertyChangeListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }
}
