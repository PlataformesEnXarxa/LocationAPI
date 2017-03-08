package udl.eps.platxarxa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;

public class LocationReceiver extends BroadcastReceiver{

    public LocationReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (LocationResult.hasResult(intent)){
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Toast.makeText(context,R.string.locChange,Toast.LENGTH_SHORT).show();
            displayLocation(latitude,longitude);
        }
    }

    private void displayLocation(double latitude, double longitude) {
        MainActivity.getInstance().updateUI(latitude,longitude);
    }
}
