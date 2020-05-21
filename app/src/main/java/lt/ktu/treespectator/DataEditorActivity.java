package lt.ktu.treespectator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class DataEditorActivity extends AppCompatActivity {

    int actionBit;
    private long id = 1;
    private Uri filePath = null;
    private ImageView treeImage;
    private String downloadLink = null;
    private boolean photoOriginal = true;
    private EditText locationArea, name, age, height, diameter, kind, description;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    LocationManager locationManager;
    LocationListener locationListener;
    UserTree userTree;
    LatLng userLatLng = new LatLng(0,0);

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Button getLocation = findViewById(R.id.chooseLocation);
        Button selectImage = findViewById(R.id.choosePhoto);
        name = findViewById(R.id.treeNameArea);
        age = findViewById(R.id.treeAgeArea);
        height = findViewById(R.id.treeHeightArea);
        diameter = findViewById(R.id.treeDiameterArea);
        kind = findViewById(R.id.treeKindArea);
        description = findViewById(R.id.treeDescriptionArea);
        treeImage = findViewById(R.id.uploadedPhoto);
        locationArea = findViewById(R.id.treeLocationArea);
        FloatingActionButton fab = findViewById(R.id.postData);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final String actionName = getIntent().getStringExtra("actionName");
        actionBit = getIntent().getIntExtra("actionBit", 0);
        userTree = (UserTree)getIntent().getSerializableExtra("Tree");


        TextView toolbarTitle = findViewById(R.id.tool_left_title);
        toolbarTitle.setText(actionName);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null || downloadLink != null || !name.getText().toString().isEmpty() || !age.getText().toString().isEmpty()
                        || !height.getText().toString().isEmpty() || !diameter.getText().toString().isEmpty()
                        || !kind.getText().toString().isEmpty() || !description.getText().toString().isEmpty()
                        || !locationArea.getText().toString().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DataEditorActivity.this);
                    builder.setTitle(getResources().getString(R.string.message));
                    builder.setMessage(getResources().getString(R.string.leave_confirmation));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            onBackPressed();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                    onBackPressed();
            }
        });

        if (!isInternetWorking())
        {
            ScrollView sw = findViewById(R.id.editorScrollView);
            sw.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(DataEditorActivity.this);
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

        if(actionBit == 1)
        {
            new DownloadImageTask(treeImage).execute(userTree.imageUrl);
            name.setText(userTree.getName());
            age.setText(userTree.getAge());
            height.setText(userTree.getHeight());
            diameter.setText(userTree.getDiameter());
            kind.setText(userTree.getKind());
            description.setText(userTree.getDescription());
            locationArea.setText(userTree.getLocation());
        }

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                selectImage();
            }
        });
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getUserLocation();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionBit == 1)
                {
                    if ((filePath != null || downloadLink != null || userTree.getImageUrl() != null)
                            && !name.getText().toString().isEmpty() && !age.getText().toString().isEmpty() && !height.getText().toString().isEmpty()
                            && !diameter.getText().toString().isEmpty() && !kind.getText().toString().isEmpty() && !description.getText().toString().isEmpty()
                            && !locationArea.getText().toString().isEmpty())
                        if (photoOriginal)
                            editData(userTree.getImageUrl());
                        else
                            uploadImage(actionBit);
                    else
                        Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.fill_values), Toast.LENGTH_LONG).show();
                }
                else
                {
                    if ((filePath != null || downloadLink != null) && !name.getText().toString().isEmpty() && !age.getText().toString().isEmpty()
                            && !height.getText().toString().isEmpty()
                            && !diameter.getText().toString().isEmpty() && !kind.getText().toString().isEmpty()
                            && !description.getText().toString().isEmpty() && !locationArea.getText().toString().isEmpty())
                            uploadImage(actionBit);
                    else
                        Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.fill_values), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TreesActivity.class);
        startActivity(intent);
        finish();
    }

    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.choose_photo)), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                treeImage.setImageBitmap(bitmap);
                photoOriginal = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final int actionBit)
    {
        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.uploading));
            progressDialog.show();

            final StorageReference ref = storageReference.child(UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            progressDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    filePath = null;
                                    downloadLink = uri.toString();
                                    if (actionBit == 0)
                                        addData(downloadLink);
                                    else
                                        editData(downloadLink);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.upload_unsuccesfull) + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void getUserLocation()
    {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                locationArea.setText(userLatLng.latitude + ", " + userLatLng.longitude);
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
        askLocationPermission();
    }

    private void askLocationPermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if (ActivityCompat.checkSelfPermission(DataEditorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DataEditorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    userLatLng = new LatLng(0, 0);
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

    private void addData(String imageLink)
    {
        final String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final String link = imageLink;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("UserTrees");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        id = dataSnapshot.getChildrenCount() + 1;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final UserTree ut = new UserTree(String.valueOf(id), name.getText().toString(), age.getText().toString(), height.getText().toString(), diameter.getText().toString(), kind.getText().toString(), currentDate, currentDate, description.getText().toString(), android_id, link, locationArea.getText().toString());
                ref.child(String.valueOf(id)).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        mutableData.setValue(ut);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot)
                    {
                        if (databaseError == null)
                        {
                            Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.upload_succesfull), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(DataEditorActivity.this, TreesActivity.class);
                            startActivity(intent);
                            finish();
                            downloadLink = null;
                        }
                        else
                            Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.information_unsuccesfull) + databaseError, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + databaseError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editData(String imageLink)
    {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final UserTree ut;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("UserTrees");
        id = Long.parseLong(userTree.getID());
        ut = new UserTree(String.valueOf(id), name.getText().toString(), age.getText().toString(), height.getText().toString(), diameter.getText().toString(), kind.getText().toString(), userTree.getStartDate(), currentDate, description.getText().toString(), android_id, imageLink, locationArea.getText().toString());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ref.child(String.valueOf(id)).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        mutableData.setValue(ut);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot)
                    {
                        if (databaseError == null)
                        {
                            Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.upload_succesfull), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(DataEditorActivity.this, TreesActivity.class);
                            startActivity(intent);
                            finish();
                            downloadLink = null;
                        }
                        else
                            Toast.makeText(DataEditorActivity.this, getResources().getString(R.string.information_unsuccesfull) + databaseError, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + databaseError, Toast.LENGTH_LONG).show();
            }
        });
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
