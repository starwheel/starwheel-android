package net.omplanet.starwheel.domain;

import android.net.Uri;

/**
 * A person.
 * @schema http://schema.org/Person
 */
public class Person extends Thing {
    //Data Properties
    private String givenName;
    private String familyName;
    private String additionalName;
    private String username;
    private String gender;
    private String age;
    private String languages;

    private String email;
    private String phone;
    private String website;

    //Object Properties
    //People
    private Uri[] personKnows;
    //Groups
    private Uri[] groupMemberOf;
    private Uri[] groupAdministratorOf;
    //Communities
    private Uri[] communityMemberOf;
    //Places
    private Uri placeHomeLocation;
    private Uri placeWorkLocation;
    private Uri placeCurrentLocation;

    public Person() {}

    public Person(Uri uri, String givenName, Uri image, String description, String[] tags, String email, Uri[] groupMemberOf) {
        super(uri, givenName, image, description, tags);
        this.email = email;
        this.groupMemberOf = groupMemberOf;
        this.givenName = givenName;
    }
}
