package net.omplanet.starwheel.domain;

import android.net.Uri;

/**
 * The most generic type of item.
 * @schema http://schema.org/Thing
 */
public class Thing {
    private Uri uri; //Unique URI of the item
    private String name;
    private String description;
    private String[] tags;

    private Uri image;

    public Thing() {}

    public Thing(Uri uri, String name, Uri image, String description, String[] tags) {
        this.uri = uri;
        this.name = name;
        this.image = image;
        this.description = description;
        this.tags = tags;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }
}
