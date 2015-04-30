package net.omplanet.starwheel.model.domain;

import java.util.Map;

/**
 * A person.
 * @schema http://schema.org/Person
 */
public class Person extends Thing {
    //Data Properties
    protected String familyName;
    protected String additionalName;
    protected String gender;
    protected String age;
    protected String birthDate;
    protected String languages;
    protected String timeZone;
    protected int state;

    //Contact
    protected String email;
    protected String phone;
    protected String website;
    protected Map<String, String> socialPages; // e.g. {"Facebook", "https://www.facebook.com/profile"}, {"Twitter", "https://..."}

    //Object Properties
    //People
    protected String[] knowsPersons;
    //Group
    protected String[] memberOfGroups;
    protected String[] ownerOfGroups;
    //Communities
    protected String[] memberOfCommunities;
    //Places
    protected Place[] places;

    /* Constructors ******************************/
    public Person() {}

    public Person(String id, String name, String image, String description, String[] tags) {
        super(id, name, image, description, tags);
    }

    /* Getters and Setters ***********************/

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Map<String, String> getSocialPages() {
        return socialPages;
    }

    public void setSocialPages(Map<String, String> socialPages) {
        this.socialPages = socialPages;
    }

    public String[] getKnowsPersons() {
        return knowsPersons;
    }

    public void setKnowsPersons(String[] knowsPersons) {
        this.knowsPersons = knowsPersons;
    }

    public String[] getMemberOfGroups() {
        return memberOfGroups;
    }

    public void setMemberOfGroups(String[] memberOfGroups) {
        this.memberOfGroups = memberOfGroups;
    }

    public String[] getOwnerOfGroups() {
        return ownerOfGroups;
    }

    public void setOwnerOfGroups(String[] ownerOfGroups) {
        this.ownerOfGroups = ownerOfGroups;
    }

    public String[] getMemberOfCommunities() {
        return memberOfCommunities;
    }

    public void setMemberOfCommunities(String[] memberOfCommunities) {
        this.memberOfCommunities = memberOfCommunities;
    }

    public Place[] getPlaces() {
        return places;
    }

    public void setPlaces(Place[] places) {
        this.places = places;
    }
}
