package net.omplanet.starwheel.model.api;

import net.omplanet.starwheel.model.domain.Flower;
import net.omplanet.starwheel.model.domain.Group;
import net.omplanet.starwheel.model.domain.Person;
import net.omplanet.starwheel.model.domain.Seed;

import java.util.HashMap;
import java.util.Map;

public class CommunityData {
    //Maps of all the member IDs with the member instances in the community
    private Map<String, Seed> allSeeds = new HashMap<String, Seed>();
    private Map<String, Flower> allFlowers = new HashMap<String, Flower>();
    private Map<String, Person> allPersons = new HashMap<String, Person>();

    /* Getters and Setters ***********************/
    public Map<String, Seed> getAllSeeds() {
        return allSeeds;
    }

    public void setAllSeeds(Map<String, Seed> allSeeds) {
        this.allSeeds = allSeeds;
    }

    public Map<String, Flower> getAllFlowers() {
        return allFlowers;
    }

    public void setAllFlowers(Map<String, Flower> allFlowers) {
        this.allFlowers = allFlowers;
    }

    public Map<String, Person> getAllPersons() {
        return allPersons;
    }

    public void setAllPersons(Map<String, Person> allPersons) {
        this.allPersons = allPersons;
    }

//TO BE USED LATER
/*    private Map<String, Group> allCommunityGroupsMap;
    private Map<String, Person> allCommunityPersonsMap;

    private void loadCommunityData() {
        //Init community group maps
        List<Group> groupsList = response.getAllGroups();
        allCommunityGroupsMap = new HashMap<String, Group>();
        for(Group group : groupsList) { allCommunityGroupsMap.put(group.getUri(), group); }

        //Init community person maps
        List<Person> personsList = response.getAllPersons();
        allCommunityPersonsMap = new HashMap<String, Person>();
        for(Person person : personsList) { allCommunityPersonsMap.put(person.getUri(), person); }
    }

    private void init() {
        //Iterate memberGroups from allCommunityGroupsMap
        for(Group group : community.getMemberGroups()) {
            switch (group.getStructureType()) {
                case SEED:
                    List<Person> personMembers = new ArrayList<Person>(7);
                    for(String memberUri: group.getMembers()) {
                        Person person = allCommunityPersonsMap.get(memberUri);
                        personMembers.add(person);
                    }

                    //TODO
                    break;
                case FLOWER:
                    List<Group> flowerOfGroups = new ArrayList<Group>(7);
                    for(String memberUri: group.getMembers()) {
                        Group seedGroup = allCommunityGroupsMap.get(memberUri);
                        flowerOfGroups.add(seedGroup);
                    }

                    //TODO
                    break;
            }

        }


    //Structure
    public GroupStructureType structureType;
    public enum GroupStructureType {
        @SerializedName("0")
        UNBOUNDED,
        @SerializedName("1")
        SEED,
        @SerializedName("2")
        FLOWER
    }

    //Get hex
                final Hex hex;
                switch (grid.shape) {
                    case HEXAGON:
                    case FLOWER_OF_LIFE:
                        hex = cube.toHex();
                        break;
                    case RECTANGLE:
                        hex = cube.cubeToOddRHex();
                        break;
                    default:
                        hex = null;
                }
    }*/
}
