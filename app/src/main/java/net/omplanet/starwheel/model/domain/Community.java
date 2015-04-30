package net.omplanet.starwheel.model.domain;

import java.util.List;

/**
 * Communities that can have many members, groups, organisations, tribes or even neighborhood or a city.
 * @schema
 */
public class Community extends Thing {
    //Data Properties
    private String createdAt;

    //Members
    protected List<String> persons; //All member persons
    protected List<String> groups; //All member groups
    protected List<String> communities; //All member communities and its subclasses

    /* Getters and Setters ***********************/
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getPersons() {
        return persons;
    }

    public void setPersons(List<String> persons) {
        this.persons = persons;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getCommunities() {
        return communities;
    }

    public void setCommunities(List<String> communities) {
        this.communities = communities;
    }
}
