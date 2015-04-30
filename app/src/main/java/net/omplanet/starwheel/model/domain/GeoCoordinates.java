package net.omplanet.starwheel.model.domain;

import com.google.gson.annotations.SerializedName;

/**
 * The geographic coordinates of a place.
 * @schema http://schema.org/GeoCoordinates
 */
public class GeoCoordinates extends Thing {
    @SerializedName("elevation")
    protected String altitude;
    protected String latitude; //For example 37.42242.
    protected String longitude; //For example -122.08585.

    /* Getters and Setters ***********************/

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
