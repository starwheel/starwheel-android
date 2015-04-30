package net.omplanet.starwheel.model.domain;

import com.google.gson.annotations.SerializedName;

/**
 * The most generic type of item.
 * @schema http://schema.org/Thing
 */
public class Thing {
    @SerializedName("@id")
    protected String id; //Unique URI of the item to be identified
    protected String name;
    protected String description;
    protected String[] tags;

    protected String image;
    protected String coverImage;

    public Thing() {}

    public Thing(String id, String name, String image, String description, String[] tags) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
}
