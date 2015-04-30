package net.omplanet.starwheel.model.api;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class APIConstants {
    public static final String API_BASE = "http://omplanet.net/api/v1.0";

    //OMPlanet API Root Nodes
    public static final String THING = "/thing";
    public static final String PERSON = "/person";
    public static final String GROUP = "/group";
    public static final String COMMUNITY = "/community";
    public static final String DATA = "/data";
    public static final String MESSAGE = "/message";

    public static String DATA_JSON = "/data.json";
    public static String IDS_JSON = "/ids.json";

    //ATTRS
    public static String USERNAME = "user_name";
    public static String ID = "id";
    public static String MAX_ID = "max_id";
    public static String COUNT = "count";

    //MESSAGE Format
    public final static String MESSAGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ"; //Twitter "EEE, dd MMM yyyy HH:mm:ss Z"
    public static final SimpleDateFormat messageSdf = new SimpleDateFormat(MESSAGE_DATE_FORMAT, Locale.US);
    static {
        messageSdf.setLenient(true);
    }
}
