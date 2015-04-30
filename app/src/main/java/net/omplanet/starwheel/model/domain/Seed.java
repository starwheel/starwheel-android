package net.omplanet.starwheel.model.domain;

import java.util.List;
import java.util.Map;

/**
 * A group of 7 persons in a seed of life structure.
 * @Schema
 */
public class Seed extends Group {
    //Members
    protected List<String> persons; //All member persons

    //Structures
    protected Map<String, String> cubeToPersonIdsStructureMap; //(String cubeCoordinate, String personId)

    /* Getters and Setters ***********************/
    public List<String> getPersons() {
        return persons;
    }

    public void setPersons(List<String> persons) {
        if(persons.size() < 1 || persons.size() > 7) {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        } else {
            this.persons = persons;
        }
    }

    public void addMember(String person) {
        if(persons.size() < 7) {
            persons.add(person);
        } else {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        }
    }

    public void removeMember(String person) {
        if(persons.size() < 2) {
            persons.remove(person);
        } else {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        }
    }

    /* Getters and Setters ***********************/

    public Map<String, String> getCubeToPersonIdsStructureMap() {
        return cubeToPersonIdsStructureMap;
    }

    public void setCubeToPersonIdsStructureMap(Map<String, String> cubeToPersonIdsStructureMap) {
        this.cubeToPersonIdsStructureMap = cubeToPersonIdsStructureMap;
    }
}
