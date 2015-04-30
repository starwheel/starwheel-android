package net.omplanet.starwheel.model.domain;

import java.util.List;
import java.util.Map;

/**
 * A group of memberPersons or memberGroups.
 * @Schema
 */
public class Group extends Thing {
    //Data Properties
    private String createdAt;

    //Contact
    protected String email;
    protected String website;
    protected Map<String, String> socialPages; // e.g. {"Facebook", "https://www.facebook.com/profile"}, {"Twitter", "https://..."}

    //Places
    protected Place[] places;

    //Object Properties
    //Members
    protected List<String> members; //All the member things, persons or groups
    //Groups
    protected List<String> memberOfGroups;
    //Communities
    protected List<String> memberOfCommunities;

    /* Constructors ******************************/
    public Group() {}

    public Group(String id, String name, String image, String description, String[] tags, List<String> members) {
        super(id, name, image, description, tags);
        this.members = members;
    }

    /* Getters and Setters ***********************/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Place[] getPlaces() {
        return places;
    }

    public void setPlaces(Place[] places) {
        this.places = places;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getMemberOfGroups() {
        return memberOfGroups;
    }

    public void setMemberOfGroups(List<String> memberOfGroups) {
        this.memberOfGroups = memberOfGroups;
    }

    public List<String> getMemberOfCommunities() {
        return memberOfCommunities;
    }

    public void setMemberOfCommunities(List<String> memberOfCommunities) {
        this.memberOfCommunities = memberOfCommunities;
    }
}
