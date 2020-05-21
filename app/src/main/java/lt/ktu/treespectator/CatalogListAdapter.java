package lt.ktu.treespectator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CatalogListAdapter extends RecyclerView.Adapter<CatalogListAdapter.ViewHolder> {

    List<CatalogTree> trees = new ArrayList<>();
    Context context;
    Intent intent;


    public CatalogListAdapter(List<CatalogTree> treesList) {
        trees = treesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.catalog_layout_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.txt_name.setText(trees.get(position).getName());
        holder.txt_latinName.setText(trees.get(position).getLatinName());
        new DownloadImageTask(holder.treeImage)
                .execute(trees.get(position).imageUrl);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent= new Intent(context, CatalogTreeActivity.class);
                intent.putExtra("Name", trees.get(position).getName());
                intent.putExtra("ImageUrl", trees.get(position).getImageUrl());
                intent.putExtra("Description", trees.get(position).getDescription());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trees.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txt_name;
        TextView txt_latinName;
        ImageView treeImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_latinName = itemView.findViewById(R.id.txt_latinName);
            treeImage = itemView.findViewById(R.id.treeImage);
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
}