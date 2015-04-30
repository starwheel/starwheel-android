package net.omplanet.starwheel.ui.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.model.domain.Flower;
import net.omplanet.starwheel.model.domain.Person;
import net.omplanet.starwheel.model.domain.Seed;
import net.omplanet.starwheel.model.domain.Thing;
import net.omplanet.starwheel.model.hexagonalgrids.Cube;
import net.omplanet.starwheel.model.hexagonalgrids.FlowerGrid;
import net.omplanet.starwheel.ui.activity.MainActivity;
import net.omplanet.starwheel.ui.view.LetterImageView;
import net.omplanet.starwheel.util.ColorUtil;

import java.util.Collection;
import java.util.List;

/**
 * A simple Fragment subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMapFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends SupportMapFragment implements OnMapReadyCallback {
    public final static String TAG = MyMapFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnMapFragmentInteractionListener mListener;
    private Activity mActivity;
    private List<Thing> mThings;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyMapFragment newInstance(String param1, String param2) {
        MyMapFragment fragment = new MyMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MyMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMapInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMapFragmentInteractionListener) activity;
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMapFragmentInteractionListener {
        public void onMapInteraction(Uri uri);
    }

    public void onMapObjectsChanged(List<Thing> things) {
        mThings = things;
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        UiSettings settings = getMap().getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setZoomControlsEnabled(true);

        map.clear();
        LatLngBounds.Builder bld = new LatLngBounds.Builder();
        if(mActivity != null && mThings != null && mThings.size() > 0) {
            if(addThingMarkersToMap(map, bld)) {
                LatLngBounds bounds = bld.build();
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
            }
        }
    }

    private boolean addThingMarkersToMap(GoogleMap map, LatLngBounds.Builder bld) {
        boolean isSomethingIncluded = false;
        for (Thing thing : mThings) {
            LatLng ll = null;
            BitmapDescriptor bitmapMarker = null;

            if(thing instanceof Person) {
                try {
                    Person person = (Person) thing;
                    double latitude = new Double(person.getPlaces()[0].getGeoCoordinates().getLatitude());
                    double longitude = new Double(person.getPlaces()[0].getGeoCoordinates().getLongitude());
                    ll = new LatLng(latitude, longitude);
//                    float hue = ColorUtil.getColorHue(Color.parseColor(thing.getColor() == null ? "darkgrey" : thing.getColor()));
//                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(hue);
//                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    bitmapMarker = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_person);
                } catch (Exception e) {
                    continue;
                }
            } else if(thing instanceof Seed) {
                try {
                    Seed seed = (Seed) thing;
                    double latitude = new Double(seed.getPlaces()[0].getGeoCoordinates().getLatitude());
                    double longitude = new Double(seed.getPlaces()[0].getGeoCoordinates().getLongitude());
                    ll = new LatLng(latitude, longitude);
                    bitmapMarker = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_seed);
                } catch (Exception e) {
                    continue;
                }
            }

            map.addMarker(new MarkerOptions()
                    .position(ll)
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .title(thing.getName())
                    .snippet(thing.getDescription())
                    .icon(bitmapMarker));

            bld.include(ll);
            isSomethingIncluded = true;
        }

        return isSomethingIncluded;
    }
}
