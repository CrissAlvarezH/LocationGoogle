package pinturas.cristian.com.locationgoogle;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
  private final String TAG = "PosicionServicio";

  private static final int COD_PERMISOS = 3452;
  private GoogleMap mMap;
  private MiReceiver miReceiver;
  private PolylineOptions polylineOptions;
  private Polyline polyline;
  private LatLng ultimaPos;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

    if(pedirPermisosFaltantes()){
      startService(new Intent(this, LocationGoogleService.class));
    }

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    polylineOptions = new PolylineOptions();
    polylineOptions.color(Color.parseColor("#990202"));

  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(8.7593615, -75.877503), 13));
    ultimaPos = new LatLng(8.7593615, -75.877503);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if(pedirPermisosFaltantes()){
      startService(new Intent(this, LocationGoogleService.class));
    }

  }

  private boolean pedirPermisosFaltantes(){
    boolean todosConsedidos = true;
    ArrayList<String> permisosFaltantes = new ArrayList<>();

    boolean permisoCoarse = ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED);

    boolean permisoFine = ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED);


    if(!permisoCoarse){
      todosConsedidos = false;
      permisosFaltantes.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    if(!permisoFine){
      todosConsedidos = false;
      permisosFaltantes.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }


    if(!todosConsedidos) {
      String[] permisos = new String[permisosFaltantes.size()];
      permisos = permisosFaltantes.toArray(permisos);

      ActivityCompat.requestPermissions(this, permisos, COD_PERMISOS);
    }

    return todosConsedidos;
  }

  @Override
  protected void onResume() {
    super.onResume();

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("NuevaPosicion");;

    miReceiver = new MiReceiver();

    LocalBroadcastManager.getInstance(this).registerReceiver(miReceiver, intentFilter);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if( miReceiver != null ){
      LocalBroadcastManager.getInstance(this).unregisterReceiver(miReceiver);
    }

  }

  private class MiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if( intent != null ){
        String accion = intent.getAction();

        if(accion != null){

          switch (accion){
            case "NuevaPosicion":
              Log.v(TAG, "onReceive");

              double latitud = intent.getDoubleExtra("latitud", 0.0);
              double longitud = intent.getDoubleExtra("longitud", 0.0);

              if( mMap != null && latitud != 0.0 && longitud != 0.0 ){
                agregarPosicion(latitud, longitud);
              }

              break;
          }

        }
      }
    }
  }

  private void agregarPosicion(double latitud, double longitud){
    // Si la posicion que viene no es la misma que la anterior
    if(ultimaPos.latitude != latitud && ultimaPos.latitude != latitud){
      ultimaPos = new LatLng(latitud, longitud);

      MarkerOptions optionsMarcadores = new MarkerOptions();
      optionsMarcadores.position( ultimaPos );

      mMap.addMarker( optionsMarcadores );

      if( polyline != null ) {
        polyline.remove();// removemos la anterior polylinea
      }

      LatLng latLng = new LatLng(latitud, longitud);

      polylineOptions.add(latLng);// añadimos un nuevo punto

      polyline = mMap.addPolyline(polylineOptions);// creamos una nueva polilinea con el nuevo punto
    }

    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitud, longitud)));

  }

}
