package udl.eps.platxarxa;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.LocationListener;

public class MainActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

    private static MainActivity instance;
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

	private Location mLastLocation;

	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;

	// boolean flag to toggle periodic location updates
	private boolean mRequestingLocationUpdates = false;

	private LocationRequest mLocationRequest;

	// Location updates intervals in sec
	private static int UPDATE_INTERVAL = 20000; // 20 sec
	private static int FATEST_INTERVAL = 5000; // 5 sec
	private static int DISPLACEMENT = 10; // 10 meters

	private static final int REQUEST_LOCATION = 2;

    private PendingIntent pendingIntent;
	// UI elements
	private TextView lblLocation;
	private Button btnShowLocation, btnStartLocationUpdates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		instance = this;
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lblLocation = (TextView) findViewById(R.id.lblLocation);
		btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
		btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);

		// First we need to check availability of play services
		if (checkPlayServices()) {

			// Building the GoogleApi client
			buildGoogleApiClient();

			createLocationRequest();
		}

		// Show location button click listener
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayLocation();
			}
		});

		// Toggling the periodic location updates
		btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				togglePeriodicLocationUpdates();
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkPlayServices()) {

			// Resuming the periodic location updates
			if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
				startLocationUpdates();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient != null)
		   if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		   }
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mGoogleApiClient != null)
		    stopLocationUpdates();
	}

	/**
	 * Method to display the location on UI
	 * */
	private void displayLocation() {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_LOCATION);
		} else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                lblLocation.setText(latitude + " , " + longitude);
            }
            else {
				Toast.makeText(this, R.string.noLocationDetect, Toast.LENGTH_LONG).show();
            }
        }

	}



	/**
	 * Method to toggle periodic location updates
	 * */
	private void togglePeriodicLocationUpdates() {
		if (!mRequestingLocationUpdates) {
			// Changing the button text
            btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));
            mRequestingLocationUpdates = true;
            startLocationUpdates();
			Log.d(TAG, "Periodic location updates started!");

		} else {
			// Changing the button text
            btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
			Log.d(TAG, "Periodic location updates stopped!");
		}
	}

	/**
	 * Creating google api client object
	 * */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	/**
	 * Creating location request object
	 * */
	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FATEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
	}

	/**
	 * Method to verify google play services on the device
	 * */
	private boolean checkPlayServices() {

		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		if(result != ConnectionResult.SUCCESS) {
			if(googleAPI.isUserResolvableError(result)) {
				googleAPI.getErrorDialog(this, result,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			return false;
		}
		return true;
	}

	/**
	 * Starting the location updates
	 * */
	protected void startLocationUpdates() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {

			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
        Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, pendingIntent);
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
	}

	/**
	 * Stopping location updates
	 */
	protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,pendingIntent);
		//LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

	}

	/**
	 * Google api callback methods
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {

		// Once connected with google api, get the location
		displayLocation();

		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// Assign the new location
		mLastLocation = location;

		Toast.makeText(getApplicationContext(), R.string.locChange,
				Toast.LENGTH_SHORT).show();

		displayLocation();

	}

    public static MainActivity getInstance(){
        return instance;
    }
    public void updateUI(final Double latitude, final Double longitude){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = latitude + " , " + longitude;
                lblLocation.setText(msg);
            }
        });
    }
}
