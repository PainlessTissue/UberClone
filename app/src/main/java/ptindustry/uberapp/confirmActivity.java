package ptindustry.uberapp;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Locale;

public class confirmActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    ParseObject rider;
    LatLng driversLocation;
    LatLng ridersLocation;

    public void confirmUber(View view)
    {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + driversLocation.latitude + "," + driversLocation.longitude + //this is filling the location with drivers location
                        "&daddr= " + ridersLocation.latitude + "," + ridersLocation.longitude)); //and this is using the riders location
        startActivity(intent); //this is sending over to google maps
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        driversLocation = new LatLng(intent.getDoubleExtra("driversLat", 0), intent.getDoubleExtra("driversLong", 0));

        int i = intent.getIntExtra("position", 90);
        double lat = driverActivity.requestLat.get(i);
        double lon = driverActivity.requestLong.get(i);

        ridersLocation = new LatLng(lat, lon);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(driversLocation).title("Drivers Location"));
        mMap.addMarker(new MarkerOptions().position(ridersLocation).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driversLocation, 15));
    }
}
