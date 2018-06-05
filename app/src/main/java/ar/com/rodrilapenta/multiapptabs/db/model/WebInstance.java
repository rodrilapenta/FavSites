package ar.com.rodrilapenta.multiapptabs.db.model;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by rodri on 3/06/2018.
 */

public class WebInstance {
    @DatabaseField
    private String name;
    @DatabaseField
    private String description;
    @DatabaseField
    private String uri;

    @DatabaseField
    private String firebaseId;

    public WebInstance(String name, String description, String uri) {
        this.name = name;
        this.description = description;
        this.uri = uri;
    }

    public WebInstance(String name, String description, String uri, String firebaseId) {
        this.name = name;
        this.description = description;
        this.uri = uri;
        this.firebaseId = firebaseId;
    }

    public WebInstance() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
