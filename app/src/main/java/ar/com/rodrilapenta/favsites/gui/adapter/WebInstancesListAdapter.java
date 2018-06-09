package ar.com.rodrilapenta.favsites.gui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;

import ar.com.rodrilapenta.favsites.R;
import ar.com.rodrilapenta.favsites.db.model.WebInstance;

public class WebInstancesListAdapter extends RecyclerView.Adapter<WebInstancesListAdapter.WebInstanceViewHolder> {
    private List<WebInstance> webInstances;
    private WebView webView;

    public static class WebInstanceViewHolder extends RecyclerView.ViewHolder {
        public TextView webInstanceName;
        public TextView webInstanceDescription;
        public TextView webInstanceUri;
        public TextView webInstanceFirebaseId;


        public WebInstanceViewHolder(final View v, final WebView webView) {
            super(v);
            webInstanceName = v.findViewById(R.id.webInstanceName);
            webInstanceDescription = v.findViewById(R.id.webInstanceDescription);
            webInstanceUri = v.findViewById(R.id.webInstanceUri);
            webInstanceFirebaseId = v.findViewById(R.id.webInstanceFirebaseId);
        }
    }

    public WebInstancesListAdapter(List<WebInstance> webInstances, WebView webView) {
        this.webInstances = webInstances;
        this.webView = webView;
    }

    @Override
    public WebInstancesListAdapter.WebInstanceViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.web_instance_item_layout, parent, false);

        return new WebInstanceViewHolder(v, webView);
    }

    @Override
    public void onBindViewHolder(WebInstanceViewHolder viewHolder, int i) {
        viewHolder.webInstanceName.setText(webInstances.get(i).getName());
        viewHolder.webInstanceDescription.setText(webInstances.get(i).getDescription());
        viewHolder.webInstanceUri.setText(webInstances.get(i).getUri());
        viewHolder.webInstanceFirebaseId.setText(webInstances.get(i).getFirebaseId());
    }

    @Override
    public int getItemCount() {
        return webInstances.size();
    }
}