package ar.com.rodrilapenta.multiapptabs.gui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ar.com.rodrilapenta.multiapptabs.R;
import ar.com.rodrilapenta.multiapptabs.db.model.WebInstance;

/**
 * Created by rodri on 4/9/17.
 */

public class WebInstancesListAdapter extends RecyclerView.Adapter<WebInstancesListAdapter.WebInstanceViewHolder> {
    private List<WebInstance> webInstances;
    private WebView webView;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class WebInstanceViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView webInstanceName;
        public TextView webInstanceDescription;
        public TextView webInstanceUri;


        public WebInstanceViewHolder(final View v, final WebView webView) {
            super(v);
            webInstanceName = v.findViewById(R.id.webInstanceName);
            webInstanceDescription = v.findViewById(R.id.webInstanceDescription);
            webInstanceUri = v.findViewById(R.id.webInstanceUri);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WebInstancesListAdapter(List<WebInstance> webInstances, WebView webView) {
        this.webInstances = webInstances;
        this.webView = webView;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WebInstancesListAdapter.WebInstanceViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.web_instance_item_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = ((TextView) view.findViewById(R.id.webInstanceUri)).getText().toString();
                webView.loadUrl(uri);
            }
        });
        return new WebInstanceViewHolder(v, webView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WebInstanceViewHolder viewHolder, int i) {
        viewHolder.webInstanceName.setText(webInstances.get(i).getName());
        viewHolder.webInstanceDescription.setText(webInstances.get(i).getDescription());
        viewHolder.webInstanceUri.setText(webInstances.get(i).getUri());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return webInstances.size();
    }
}