package lt.ktu.treespectator;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomMarker implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomMarker(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.marker_info, null);

        TextView name = view.findViewById(R.id.name);
        TextView height = view.findViewById(R.id.height);
        TextView diameter = view.findViewById(R.id.diameter);
        TextView age = view.findViewById(R.id.age);
        TextView kind = view.findViewById(R.id.kind);
        TextView startdate = view.findViewById(R.id.startdate);


        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        name.setText(name.getText() + " " + infoWindowData.getName());
        String sourceString = "<b>" + height.getText() + "</b> " + infoWindowData.getHeight();
        height.setText(Html.fromHtml(sourceString));
        sourceString = "<b>" + diameter.getText() + "</b> " + infoWindowData.getDiameter();
        diameter.setText(Html.fromHtml(sourceString));
        sourceString = "<b>" + age.getText() + "</b> " + infoWindowData.getAge();
        age.setText(Html.fromHtml(sourceString));
        sourceString = "<b>" + kind.getText() + "</b> " + infoWindowData.getKind();
        kind.setText(Html.fromHtml(sourceString));
        sourceString = "<b>" + startdate.getText() + "</b> " + infoWindowData.getStartDate();
        startdate.setText(Html.fromHtml(sourceString));

        return view;
    }
}