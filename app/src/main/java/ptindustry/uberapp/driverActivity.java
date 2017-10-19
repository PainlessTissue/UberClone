package ptindustry.uberapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class driverActivity extends AppCompatActivity
{

    LocationListener locationListener;
    LocationManager locationManager;
    Location lastKnownLocation;
    static ArrayList<String> ridersLocationArray;

    static ArrayList<Double> requestLat = new ArrayList<>();
    static ArrayList<Double> requestLong = new ArrayList<>();

    public void locationShit()
    {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                lastKnownLocation = location;
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
            }
        }

        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        final ListView listView = (ListView) findViewById(R.id.listView);

        ridersLocationArray = new ArrayList<>();
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Request");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, ridersLocationArray);
        listView.setAdapter(arrayAdapter);

        locationShit();



        final ParseGeoPoint driversLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        parseQuery.whereNear("location", driversLocation);

        parseQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if(e == null)
                {
                    if (objects.size() > 0)
                    {
                        for (ParseObject object : objects)
                        {
                            ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");

                            if(requestLocation != null)
                            {
                                ridersLocationArray.add(String.valueOf( //adding to arraylist
                                        Math.round( //round numbers
                                                driversLocation.distanceInMilesTo(object.getParseGeoPoint("location")) //the actual distance between them
                                                        * 10) / 10) + " miles");  //the rounding

                                requestLat.add(requestLocation.getLatitude());
                                requestLong.add(requestLocation.getLongitude());
                            }

                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), confirmActivity.class);

                //intent.putExtra("longitudeArr", requestLong);
                //intent.putExtra("latitudeArr", requestLat);
                intent.putExtra("position", i);

                intent.putExtra("driversLat", driversLocation.getLatitude());
                intent.putExtra("driversLong", driversLocation.getLongitude());

                startActivity(intent);
            }
        });
    }
}
