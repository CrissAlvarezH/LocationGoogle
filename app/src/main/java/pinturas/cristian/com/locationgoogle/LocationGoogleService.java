package pinturas.cristian.com.locationgoogle;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

public class LocationGoogleService extends Service {
  private final String TAG = "PosicionServicio";

  private FusedLocationProviderClient miFusedLocationCliente;
//  private TimerTaskCapturarPosicion timerTaskCapturarPosicion;

  public LocationGoogleService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

    miFusedLocationCliente = LocationServices.getFusedLocationProviderClient(this);

    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(20 * 1000);
    locationRequest.setFastestInterval(1000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      == PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

      miFusedLocationCliente.requestLocationUpdates(locationRequest, new LocationLlamada(), null);
    }

    // Comentamos el Timer Task para dejar solo el servicio de LocationSerivice capturando posiciones
//    Timer timerImagenes = new Timer();
//    timerTaskCapturarPosicion = new TimerTaskCapturarPosicion(getApplicationContext());
//    timerImagenes.scheduleAtFixedRate(timerTaskCapturarPosicion, (10 * 1000), (10 * 1000));
  }

  private class LocationLlamada extends LocationCallback {
    @Override
    public void onLocationResult(LocationResult locationResult) {
      super.onLocationResult(locationResult);

      if(locationResult != null){
        Log.v(TAG, "onLocationResult ejecutada");

        for( Location location : locationResult.getLocations() ){
          Toast.makeText(getApplicationContext(), "Posicion recivida", Toast.LENGTH_SHORT).show();

          Log.v(TAG, "iteracion de un ciclo "+location.getLatitude()+", "+location.getLongitude());

          Intent intent = new Intent("NuevaPosicion");
          intent.putExtra("latitud", location.getLatitude());
          intent.putExtra("longitud", location.getLongitude());
          LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
      }

    }
  }

//  private class TimerTaskCapturarPosicion extends TimerTask implements OnSuccessListener<Location> {
//    private Context context;
//
//    public TimerTaskCapturarPosicion(Context context){
//      this.context = context;
//    }
//
//    @Override
//    public void run() {
//      Log.v(TAG, "Ejecicion del Run");
//
//      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//        == PackageManager.PERMISSION_GRANTED &&
//        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//        miFusedLocationCliente.getLastLocation()
//          .addOnSuccessListener(this);
//      }
//    }
//
//    @Override
//    public void onSuccess(Location location) {
//      if(location != null) {
//
//        Intent intent = new Intent("NuevaPosicion");
//        intent.putExtra("latitud", location.getLatitude());
//        intent.putExtra("longitud", location.getLongitude());
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//        Log.v(TAG, "Ultima posicion capturada " + location.getLatitude() + ", " + location.getLongitude());
//      }else{
//        Log.e(TAG, "Ultima posicion capturada nula ");
//      }
//    }
//  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

//    if(timerTaskCapturarPosicion != null){
//      timerTaskCapturarPosicion.cancel();
//    }
  }
}
