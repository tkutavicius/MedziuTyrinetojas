package lt.ktu.treespectator;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TreeInfoActivity extends AppCompatActivity {

    UserTree ut = new UserTree();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ut = (UserTree)getIntent().getSerializableExtra("Tree");
        TextView toolbarTitle = findViewById(R.id.tool_left_title);
        toolbarTitle.setText(ut.getName());

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = findViewById(R.id.editInfo);
        TextView height = findViewById(R.id.treeHeight);
        TextView diameter = findViewById(R.id.treeDiameter);
        TextView age = findViewById(R.id.treeAge);
        TextView kind = findViewById(R.id.treeKind);
        TextView startDate = findViewById(R.id.treeStartDate);
        TextView editedDate = findViewById(R.id.treeEdited);
        ImageView image = findViewById(R.id.treeImage);
        TextView info = findViewById(R.id.treeInfo);
        LinearLayout ll = findViewById(R.id.linearInfo);
        final ImageView deleteRecord = findViewById(R.id.action_delete);

        if(isInternetWorking()) {
            info.setText(ut.getDescription());
            new DownloadImageTask(image).execute(ut.imageUrl);

            String sourceString = "<b>" + height.getText() + "</b> " + ut.getHeight();
            height.setText(Html.fromHtml(sourceString));
            sourceString = "<b>" + diameter.getText() + "</b> " + ut.getDiameter();
            diameter.setText(Html.fromHtml(sourceString));
            sourceString = "<b>" + age.getText() + "</b> " + ut.getAge();
            age.setText(Html.fromHtml(sourceString));
            sourceString = "<b>" + kind.getText() + "</b> " + ut.getKind();
            kind.setText(Html.fromHtml(sourceString));
            sourceString = "<b>" + startDate.getText() + "</b> " + ut.getStartDate();
            startDate.setText(Html.fromHtml(sourceString));
            sourceString = "<b>" + editedDate.getText() + "</b> " + ut.getEdited();
            editedDate.setText(Html.fromHtml(sourceString));

            deleteRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TreeInfoActivity.this);
                    builder.setTitle(getResources().getString(R.string.message));
                    builder.setMessage(getResources().getString(R.string.delete_confirmation));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            deleteRecord(ut.getID());
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
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(TreeInfoActivity.this, DataEditorActivity.class);
                    intent.putExtra("actionName", getString(R.string.edit_tree));
                    intent.putExtra("actionBit", 1);
                    intent.putExtra("Tree", ut);
                    startActivity(intent);
                    finish();
                }
            });
        }
        else
        {
            fab.setVisibility(View.GONE);
            ll.setVisibility(View.GONE);
            height.setVisibility(View.GONE);
            diameter.setVisibility(View.GONE);
            age.setVisibility(View.GONE);
            kind.setVisibility(View.GONE);
            startDate.setVisibility(View.GONE);
            editedDate.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            info.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(TreeInfoActivity.this);
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

    private void deleteRecord(final String id)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("UserTrees");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.child(id).getRef().removeValue();
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.record_deleted), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(TreeInfoActivity.this, TreesActivity.class);
                startActivity(intent);
                finish();
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
}
