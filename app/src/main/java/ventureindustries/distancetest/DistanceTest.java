package ventureindustries.distancetest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DistanceTest extends AppCompatActivity {

    private TextView latitudeView;
    private TextView longitudeView;
    private Button startButton;
    private Button stopButton;
    private TextView resultView;
    private TextView readyText;
    private double longitude;
    private double latitude;
    private LocationManager locationManager;
    private Location startLocation;
    private Location stopLocation;
    private LocationListener locationListener;
    private String locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_test);

        // Attach UI Objects
        latitudeView = (TextView) findViewById(R.id.latitude_field);
        longitudeView = (TextView) findViewById(R.id.longitude_field);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        resultView = (TextView) findViewById(R.id.result_field);
        readyText = (TextView) findViewById(R.id.readyText);

        stopButton.setEnabled(false);

        // Attach Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;


        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitudeView.setText(String.valueOf(location.getLatitude()));
                longitudeView.setText(String.valueOf(location.getLongitude()));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Start Retrieving Location Updates
        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        // Create Callbacks
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Recording
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    return;
                }
                startLocation = locationManager.getLastKnownLocation(locationProvider);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop Recording and Display Result
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                stopLocation = locationManager.getLastKnownLocation(locationProvider);


//                locationManager.removeUpdates(locationListener);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }
}
