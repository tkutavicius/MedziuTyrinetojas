package lt.ktu.treespectator;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static android.view.View.GONE;

public class CatalogTreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_tree);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isInternetWorking())
        {
            ImageView image = findViewById(R.id.treeImage);
            image.setVisibility(GONE);
            ScrollView scroller = findViewById(R.id.scroller);
            scroller.setVisibility(GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(CatalogTreeActivity.this);
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

        String name = getIntent().getStringExtra("Name");
        String description = getIntent().getStringExtra("Description");
        String imageUrl = getIntent().getStringExtra("ImageUrl");
        TextView toolbarTitle = findViewById(R.id.tool_left_title);
        toolbarTitle.setText(name);

        TextView treeDescription = findViewById(R.id.treeInfo);
        treeDescription.setText(description);

        ImageView toolBarBtn = findViewById(R.id.action_back);
        toolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        new DownloadImageTask((ImageView) findViewById(R.id.treeImage))
                .execute(imageUrl);
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
