package net.omplanet.starwheel.model.domain;

import java.util.Map;

/**
 * An organization such as a school, NGO, corporation, club, etc.
 * @schema http://schema.org/Organization
 */
public class Organisation extends Community {

    //Contact
    protected String email;
    protected String phone;
    protected String website;
    protected Map<String, String> socialPages; // e.g. {"Facebook", "https://www.facebook.com/profile"}, {"Twitter", "https://..."}

    //Places
    protected Place[] places;
}
