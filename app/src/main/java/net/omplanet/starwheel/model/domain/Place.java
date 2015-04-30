package net.omplanet.starwheel.model.domain;

/**
 * Entities that have a somewhat fixed, physical extension.
 * @Thing.name represents the locationName (e.g. home, work location, current location, etc.)
 * @Thing.description tells about the place
 * @schema http://schema.org/Place
 */
public class Place extends Thing {
    protected String address;
    protected GeoCoordinates geoCoordinates;

    /* Getters and Setters ***********************/
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoCoordinates getGeoCoordinates() {
        return geoCoordinates;
    }

    public void setGeoCoordinates(GeoCoordinates geoCoordinates) {
        this.geoCoordinates = geoCoordinates;
    }
}
