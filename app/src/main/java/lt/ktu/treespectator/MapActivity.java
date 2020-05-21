package lt.ktu.treespectator;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLatLng = new LatLng(0,0);
    List<UserTree> trees = new ArrayList<>();
    private Map<Marker, InfoWindowData> allMarkersMap = new HashMap<Marker, InfoWindowData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (isInternetWorking()) {
            mapFragment.getMapAsync(MapActivity.this);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle(getResources().getString(R.string.message));
            builder.setMessage(getResources().getString(R.string.bad_connection));
            builder.setCancelable(false);
            builder.setNegativeButton(
                    getResources().getString(R.string.go_back),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            onBackPressed();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                UserTree treeToReturn = null;
                InfoWindowData markerInfo = allMarkersMap.get(marker);
                for (UserTree t : trees)
                    if (t.getID().equals(markerInfo.getID()))
                        treeToReturn = t;
                Intent intent = new Intent(MapActivity.this, TreeInfoActivity.class);
                intent.putExtra("Tree", treeToReturn);
                startActivity(intent);
            }
        });
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        askLocationPermission(googleMap);
        getTrees(googleMap);
    }

    public void askLocationPermission(final GoogleMap googleMap) {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastLocation = null;
                if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null)
                {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } else if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (lastLocation == null)
                {
                    userLatLng = new LatLng(55.330248, 23.907066);
                }
                else
                {
                    userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void getTrees(final GoogleMap googleMap)
    {
        final DatabaseReference ref;
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        ref = FirebaseDatabase.getInstance().getReference().child("UserTrees");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                trees.clear();
                for (DataSnapshot ds : data.getChildren()) {
                    if (ds.child("owner").getValue().toString().equals(android_id))
                    {
                        String id = ds.child("id").getValue(String.class);
                        String age = ds.child("age").getValue(String.class);
                        String description = ds.child("description").getValue(String.class);
                        String diameter = ds.child("diameter").getValue(String.class);
                        String edited = ds.child("edited").getValue(String.class);
                        String height = ds.child("height").getValue(String.class);
                        String imageUrl = ds.child("imageUrl").getValue(String.class);
                        String kind = ds.child("kind").getValue(String.class);
                        String location = ds.child("location").getValue(String.class);
                        String name = ds.child("name").getValue(String.class);
                        String owner = ds.child("owner").getValue(String.class);
                        String startDate = ds.child("startDate").getValue(String.class);
                        trees.add(new UserTree(id, name, age, height, diameter, kind, startDate, edited, description, owner, imageUrl, location));
                    }
                }
                setMarkers(googleMap);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setMarkers(GoogleMap googleMap)
    {
        for (final UserTree ut : trees)
        {
            String lat = ut.getLocation().substring(0, ut.getLocation().indexOf(','));
            String lng = ut.getLocation().substring(ut.getLocation().indexOf(' '));
            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
            InfoWindowData info = new InfoWindowData();
            info.setName(ut.getName());
            info.setHeight(ut.getHeight());
            info.setDiameter(ut.getDiameter());
            info.setAge(ut.getAge());
            info.setKind(ut.getKind());
            info.setStartDate(ut.getStartDate());
            info.setID(ut.getID());
            CustomMarker customInfoWindow = new CustomMarker(MapActivity.this);
            googleMap.setInfoWindowAdapter(customInfoWindow);
            Marker m = googleMap.addMarker(markerOptions);
            m.setTag(info);
            allMarkersMap.put(m, info);
            googleMap.addMarker(markerOptions);
        }
    }

    public boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}
