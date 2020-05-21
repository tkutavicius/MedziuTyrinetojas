package lt.ktu.treespectator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CatalogActivity extends AppCompatActivity {

    List<CatalogTree> trees = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        if (isInternetWorking())
            initData();
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(CatalogActivity.this);
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

    private void initData() {
        final DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("CatalogTrees");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                trees.clear();
                for (DataSnapshot ds : data.getChildren()) {
                    String name = ds.child("Name").getValue(String.class);
                    String nameLatin = ds.child("NameLatin").getValue(String.class);
                    String image = ds.child("ImageUrl").getValue(String.class);
                    String description = ds.child("Description").getValue(String.class);
                    trees.add(new CatalogTree(name, nameLatin, description, image));
                }
                final CatalogListAdapter catalog = new CatalogListAdapter(trees);
                recyclerView.setAdapter(catalog);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.database_error) + " " + error, Toast.LENGTH_LONG).show();
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
}
