package lt.ktu.treespectator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TreesActivity extends AppCompatActivity {

    List<UserTree> trees = new ArrayList<>();

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trees);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        if (isInternetWorking())
            initData();
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(TreesActivity.this);
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

        FloatingActionButton fab = findViewById(R.id.addTree);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TreesActivity.this, DataEditorActivity.class);
                intent.putExtra("actionName", getString(R.string.add_tree));
                intent.putExtra("actionBit", 0);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
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

                final TreeListAdapter userTrees= new TreeListAdapter(trees);
                recyclerView.setAdapter(userTrees);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
