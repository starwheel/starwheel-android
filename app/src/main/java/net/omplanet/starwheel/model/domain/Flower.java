package net.omplanet.starwheel.model.domain;

import java.util.List;
import java.util.Map;

/**
 * A group of 7 groups in a flower of life structure.
 * @Schema
 */
public class Flower extends Group {
    //Members
    protected List<String> seeds; //The 7 member seeds

    //Structures
    protected Map<String, String> cubeToSeedIdsStructureMap; //e.g. (String cubeCoordinateHash, String seedId), or (String order, String seedId)
    protected Map<String, String> cubeToPersonIdsStructureMap; //(String cubeCoordinate, String personId)

    /* Getters and Setters ***********************/
    public List<String> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<String> seeds) {
        if(seeds.size() < 1 || seeds.size() > 7) {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        } else {
            this.seeds = seeds;
        }
    }

    public void addMember(String member) {
        if(seeds.size() < 7) {
            seeds.add(member);
        } else {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        }
    }

    public void removeMember(String member) {
        if(seeds.size() < 2) {
            seeds.remove(member);
        } else {
            throw new IllegalArgumentException("The count of group members violates the group structure.");
        }
    }

    /* Getters and Setters ***********************/

    public Map<String, String> getCubeToSeedIdsStructureMap() {
        return cubeToSeedIdsStructureMap;
    }

    public void setCubeToSeedIdsStructureMap(Map<String, String> cubeToSeedIdsStructureMap) {
        this.cubeToSeedIdsStructureMap = cubeToSeedIdsStructureMap;
    }

    public Map<String, String> getCubeToPersonIdsStructureMap() {
        return cubeToPersonIdsStructureMap;
    }

    public void setCubeToPersonIdsStructureMap(Map<String, String> cubeToPersonIdsStructureMap) {
        this.cubeToPersonIdsStructureMap = cubeToPersonIdsStructureMap;
    }
}
