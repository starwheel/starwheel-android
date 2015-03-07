package net.omplanet.starwheel.api;

import android.content.Context;

import net.omplanet.starwheel.domain.Person;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Demo API to get Person objects.
 */
public class People {

    public static Person[] getPeople() {
        Person[] people = null;

        try {
            /*Person[] demoPeople =
                new Person[] {
                    new Person(new URL("omplanet.net/members/cat"), "Cat", new URL("data/profile1.png"),
                                "Description", new String[] {"tag1", "tag2", "tag3"}, "cat@email.com",
                                new URL[] {new URL("group1"), new URL("tag2")}),
                    new Person(),
                    new Person(),
                    new Person(),
                    new Person()
            };*/

//            people = demoPeople;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return people;
    }

    private String loadJSONFromAsset(URL url, Context context) {
        String json = null;
        try {
            String fileName = "file:///android_asset/"+ url.getPath() + ".json";

            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}