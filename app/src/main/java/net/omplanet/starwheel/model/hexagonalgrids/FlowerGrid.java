package net.omplanet.starwheel.model.hexagonalgrids;

import net.omplanet.starwheel.model.domain.Community;
import net.omplanet.starwheel.model.domain.Flower;
import net.omplanet.starwheel.model.domain.Person;
import net.omplanet.starwheel.model.domain.Seed;

import java.util.Map;

/**
 * Custom Grid class to hold data of the Flower model with its Person Seed nodes.
 */
public class FlowerGrid extends Grid {
    //Loaded community data
    private Map<String, Person> persons; //<Cube, personId>
    private Map<String, Seed> seeds; //<Cube, personId>
    private Flower flower; //<Cube, personId>

    //Grid nodes
    private Cube[] gridNodes;

    //Structure maps
    private Map<String, String> cubeToPersonIdsStructureMap;
    private Map<String, String> cubeToSeedIdsStructureMap;

    //Init the flower model with its grid data
    public FlowerGrid(int gridRadius, int hexRadius, Grid.Shape shape, boolean orientation, Map<String, Person> persons, Map<String, Seed> seeds, Flower flower) {
        super(gridRadius, hexRadius, shape, orientation);
        this.gridNodes = StorageMap.generateNodes(this, true);

        this.persons = persons;
        this.seeds = seeds;
        this.flower = flower;

        this.cubeToPersonIdsStructureMap = flower.getCubeToPersonIdsStructureMap();
        this.cubeToSeedIdsStructureMap = flower.getCubeToSeedIdsStructureMap();
    }

    public Person getPersonByCube(Cube personCube) {
        //doing the .add(relativeCenter) to shift the cube by relative distance from (0,0,0) to relativeCenter
        String personId = cubeToPersonIdsStructureMap.get(personCube.toString());
        return persons.get(personId);
    }

    public Person getPersonByCube(Cube personCube, Cube relativeCenter) {
        //doing the .add(relativeCenter) to shift the cube by relative distance from (0,0,0) to relativeCenter
        String personId = cubeToPersonIdsStructureMap.get(personCube.add(relativeCenter).toString());
        return persons.get(personId);
    }

    public Seed getSeedByCube(Cube seedCube) {
        //doing the .add(relativeCenter) to shift the cube by relative distance from (0,0,0) to relativeCenter
        String seedId = cubeToSeedIdsStructureMap.get(seedCube.toString());
        return seeds.get(seedId);
    }

    public Seed getSeedByCube(Cube seedCube, Cube relativeCenter) {
        //doing the .add(relativeCenter) to shift the cube by relative distance from (0,0,0) to relativeCenter
        String seedId = cubeToSeedIdsStructureMap.get(seedCube.add(relativeCenter).toString());
        return seeds.get(seedId);
    }

    public static Cube getCentralPersonCube(Cube seedCube) {
        return seedCube.scale(3);
    }

    public static int getPersonsPositionInSeed(Cube personCube, Cube seedCube) {
        Cube centerCube = getCentralPersonCube(seedCube);

        if(new Cube(0,0,0).add(centerCube).equals(personCube)) {
            return 0;
        } else for(int n = 0; n < Cube.directionsClockwise.length; n++) {
            if(Cube.directionsClockwise[n].add(centerCube).equals(personCube))  { return n+1; }
        }
        return -1;
    }

    public static Cube getCentralPersonCube(Cube seedCube, Cube relativeCenter) {
        Cube centralCube = seedCube.scale(3);
        return centralCube.add(relativeCenter);
    }

    public void setGridRadius(int gridRadius) {
        this.gridRadius = gridRadius;
        this.gridNodes = StorageMap.generateNodes(this, true);
    }

    /* Getters and Setters ***********************/
    public int getGridRadius() {
        return gridRadius;
    }

    public Map<String, Person> getPersons() {
        return persons;
    }

    public void setPersons(Map<String, Person> persons) {
        this.persons = persons;
    }

    public Map<String, Seed> getSeeds() {
        return seeds;
    }

    public void setSeeds(Map<String, Seed> seeds) {
        this.seeds = seeds;
    }

    public Flower getFlower() {
        return flower;
    }

    public void setFlower(Flower flower) {
        this.flower = flower;
    }

    public Cube[] getGridNodes() {
        return gridNodes;
    }

    public void setGridNodes(Cube[] gridNodes) {
        this.gridNodes = gridNodes;
    }
}