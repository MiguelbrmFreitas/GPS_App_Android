package com.br.gpsapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements LocationListener{
    private static final int MIN_DELTA_TIME = 1000 * 10;

    private TextView tvLatLng;
    private TextView tvStatus;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try{
            //Tempo e Distancia
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
        }
        catch(SecurityException e){
            Toast.makeText(MainActivity.this, "Erro ao estimar a coordenada", Toast.LENGTH_SHORT).show();
        }

        tvLatLng = (TextView) findViewById(R.id.tvLatLng);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // Uma nova localiza��o � sempre melhor do que nenhuma localiza��o...
            return true;
        }

        // Verifica se uma localiza��o � mais recente ou mais antiga do que a localiza��o que
        // j� temos.
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MIN_DELTA_TIME;
        boolean isSignificantlyOlder = timeDelta < -MIN_DELTA_TIME;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            // J� tem mais de MIN_DELTA_TIME desde a �ltima localiza��o. Portanto, use a nova
            // localiza��o porque o usu�rio ja deve ter se movido.
            return true;
        } else if (isSignificantlyOlder) {
            // Se a nova localiza��o � MIN_DELTA_TIME mais antiga do que a que j� temos,
            // ela deve ser pior... n�o a use
            return false;
        }

        // Verifica se a nova localiza�ao � mais ou menos precisa do que a que j� temos
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Verifica se a nova localiza��o e a antiga pertencem a um mesmo provedor
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determina a qualidade da nova localiza��o usando uma combina��o de par�metros de
        // tempo e precis�o.
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    /** Verifica se dois provedores de localia��o s�o iguais */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, this.location)){
            this.location = location;

            tvLatLng.setText("Lat: " + location.getLatitude() + "\nLong: " + location.getLongitude() +
                    "\nAlt: " + location.getAltitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch(status){
            case LocationProvider.AVAILABLE:
                tvStatus.setText("GPS Disponível");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                tvStatus.setText("GPS Temporariamente Indisponível");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                tvStatus.setText("GPS Indisponível");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}