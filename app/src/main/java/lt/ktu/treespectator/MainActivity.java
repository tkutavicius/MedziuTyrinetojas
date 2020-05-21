package lt.ktu.treespectator;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (!isInternetWorking())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.message));
            builder.setMessage(getResources().getString(R.string.no_internet));
            builder.setCancelable(true);
            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            changeLanguage();
            addNewUser();
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        setSupportActionBar(toolbar);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_main:
                            return true;
                        case R.id.navigation_trees:
                            Intent intent = new Intent(MainActivity.this, TreesActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        case R.id.navigation_map:
                            intent = new Intent(MainActivity.this, MapActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        case R.id.navigation_catalog:
                            intent = new Intent(MainActivity.this, CatalogActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewUser ()
    {
        final String android_id = Secure.getString(this.getContentResolver(),
                Secure.ANDROID_ID);
        final DatabaseReference ref;
        final User user = new User(Resources.getSystem().getConfiguration().locale.getLanguage());
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(android_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (!data.exists()) {
                    ref.child(android_id).setValue(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeLanguage()
    {
        final String android_id = Secure.getString(this.getContentResolver(),
                Secure.ANDROID_ID);

        final DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("Users").child(android_id);
        user.child("language").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.getValue(String.class) != null) {
                    String language = data.getValue(String.class);
                    setAppLocale(language);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAppLocale(String localeCode){

        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
        onConfigurationChanged(config);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        BottomNavigationView bn;

        super.onConfigurationChanged(newConfig);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.main_activity));
        setSupportActionBar(toolbar);
        bn = findViewById(R.id.bottom_navigation);
        Menu menu = bn.getMenu();
        MenuItem main = menu.findItem(R.id.navigation_main);
        main.setTitle(getResources().getString(R.string.menu_main));
        MenuItem trees = menu.findItem(R.id.navigation_trees);
        trees.setTitle(getResources().getString(R.string.menu_trees));
        MenuItem map = menu.findItem(R.id.navigation_map);
        map.setTitle(getResources().getString(R.string.menu_map));
        MenuItem catalog = menu.findItem(R.id.navigation_catalog);
        catalog.setTitle(getResources().getString(R.string.menu_catalog));
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
