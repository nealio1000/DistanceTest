package ventureindustries.distancetest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    private Button networkButton;
    private Button bothButton;
    private Button gpsButton;
    private TextView resultView;
    private TextView readyText;
    private double longitude;
    private double latitude;
    private LocationManager locationManager;
    private Location startLocation;
    private Location stopLocation;
    private Location lastKnown;
    private LocationListener locationListener;
    private String locationProvider;
    private boolean firstLocationFlag = false;

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
        readyText = (TextView) findViewById(R.id.ready_text);
        networkButton = (Button) findViewById(R.id.network_button);
        bothButton = (Button) findViewById(R.id.both_button);
        gpsButton = (Button) findViewById(R.id.gps_button);

        // default button scheme on startup
        startButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Attach Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if(isBetterLocation(location, lastKnown)) {
                    latitudeView.setText(String.valueOf(location.getLatitude()));
                    longitudeView.setText(String.valueOf(location.getLongitude()));

                    if(firstLocationFlag) {
                        startLocation = location;
                        firstLocationFlag = false;
                    }
                    lastKnown = location;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };


        // Create Callbacks
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Start Recording
             */
            public void onClick(View v) {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                networkButton.setEnabled(false);
                bothButton.setEnabled(false);
                gpsButton.setEnabled(false);

                // set flag to tell onLocationChanged() to
                // save its last location as the startLocation
                firstLocationFlag = true;
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Stop recording and reset activity
             */
            public void onClick(View v) {
                // Stop Recording and Display Result
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                // Stop periodic location updates
                locationManager.removeUpdates(locationListener);

                // Calculate distance between startLocation and lastKnown and store in results[]
                float[] results = new float[3];
                Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                        lastKnown.getLatitude(), lastKnown.getLongitude(), results);

                // display results
                resultView.setText((String.valueOf(results[0])));

                // reset activity
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                networkButton.setEnabled(true);
                bothButton.setEnabled(true);
                gpsButton.setEnabled(true);
                readyText.setText(R.string.notReady);
                readyText.setTextColor(Color.rgb(255, 0, 0));
            }
        });
        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup UI
                networkButton.setEnabled(false);
                bothButton.setEnabled(true);
                gpsButton.setEnabled(true);
                resultView.setText(R.string.zero);

                // Set up location management for NETWORK_PROVIDER
                locationProvider = locationManager.NETWORK_PROVIDER;
                lastKnown = locationManager.getLastKnownLocation(locationProvider);
                latitudeView.setText(String.valueOf(lastKnown.getLatitude()));
                longitudeView.setText(String.valueOf(lastKnown.getLongitude()));

                // Start Retrieving Location Updates from NETWORK_PROVIDER
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                // Inform user that they can begin recording
                readyText.setText(R.string.ready);
                readyText.setTextColor(Color.rgb(0, 255, 0));
                startButton.setEnabled(true);
            }
        });
        bothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkButton.setEnabled(true);
                bothButton.setEnabled(false);
                gpsButton.setEnabled(true);
                startButton.setEnabled(true);
                resultView.setText(R.string.zero);

                //TODO Handle the blended case if possible

            }
        });
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup UI
                networkButton.setEnabled(true);
                bothButton.setEnabled(true);
                gpsButton.setEnabled(false);
                resultView.setText(R.string.zero);

                // setup location management for GPS_PROVIDER
                locationProvider = LocationManager.GPS_PROVIDER;
                lastKnown = locationManager.getLastKnownLocation(locationProvider);
                latitudeView.setText(String.valueOf(lastKnown.getLatitude()));
                longitudeView.setText(String.valueOf(lastKnown.getLongitude()));

                // Start Retrieving Location Updates
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                // Inform user that they can begin recording
                readyText.setText(R.string.ready);
                readyText.setTextColor(Color.rgb(0, 255, 0));
                startButton.setEnabled(true);
            }
        });

    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
