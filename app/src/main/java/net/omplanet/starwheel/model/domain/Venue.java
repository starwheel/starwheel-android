package net.omplanet.starwheel.model.domain;

import java.util.Map;

/**
 * A specific venue.
 * @schema http://schema.org/Place
 */
public class Venue extends Place {

    //Contact
    protected String email;
    protected String phone;
    protected String website;
    protected Map<String, String> socialPages; // e.g. {"Facebook", "https://www.facebook.com/profile"}, {"Twitter", "https://..."}

}
