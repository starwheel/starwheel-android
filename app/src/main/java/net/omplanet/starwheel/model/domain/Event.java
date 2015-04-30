package net.omplanet.starwheel.model.domain;

/**
 * An event related to a person, group, community or a place. .
 * @Schema
 */
public class Event extends Thing {
    protected String startDate;
    protected String endDate;
    protected String organiserId; //person, group or community
    protected Place place;
}