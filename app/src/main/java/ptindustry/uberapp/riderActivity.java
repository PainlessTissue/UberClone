package ptindustry.uberapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class riderActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean uberCalled = false;
    Button uberButton;
    Location lastKnownLocation;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                updateMap(lastKnownLocation);
            }
    }


    public void callUber(View view)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        else
        {
            Toast.makeText(this, "Your Location is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if(uberCalled == false) //if youre requesting an uber
        {
            if(lastKnownLocation != null)
            {
                ParseObject userLocationObject = new ParseObject("Request"); //make the object
                ParseGeoPoint parseGeo = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()); //get their location
                userLocationObject.put("username", ParseUser.getCurrentUser()); //assign the current user
                userLocationObject.put("location", parseGeo); //assign the data

                userLocationObject.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if (e == null)
                            uberCalled = true;
                            uberButton.setText("Cancel Uber");
                    }
                });
            }

            else
                Toast.makeText(this, "Error, try again later", Toast.LENGTH_SHORT).show();
        }

        else //if youre currently requesting an uber and want to cancel it
        {
            uberCalled = false;
            uberButton.setText("Call Uber");

            ParseQuery<ParseObject> query = new ParseQuery<>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser());

            query.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> objects, ParseException e)
                {
                    if (e == null)
                    {
                        if (objects.size() > 0)
                        {
                            for (ParseObject obj : objects)
                            {
                                obj.deleteInBackground();
                            }
                        }
                    }
                }
            });
        }
    }

    public void updateMap(Location location)
    {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your location"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        uberButton = findViewById(R.id.callUberButton);
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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                updateMap(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

            }
        };

        //requesting permission at the beginning
        if (Build.VERSION.SDK_INT >= 23) //check the version
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //make sure we have permission before continuing

            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastKnownLocation != null)
                    updateMap(lastKnownLocation);
            }
        }

        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}
