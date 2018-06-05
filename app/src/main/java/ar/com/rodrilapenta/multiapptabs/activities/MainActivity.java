package ar.com.rodrilapenta.multiapptabs.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.com.rodrilapenta.multiapptabs.R;
import ar.com.rodrilapenta.multiapptabs.controller.SessionManager;
import ar.com.rodrilapenta.multiapptabs.controller.asynctasks.ImageLoadTask;
import ar.com.rodrilapenta.multiapptabs.controller.listeners.RecyclerTouchListener;
import ar.com.rodrilapenta.multiapptabs.db.model.WebInstance;
import ar.com.rodrilapenta.multiapptabs.gui.adapter.WebInstancesListAdapter;
import ar.com.rodrilapenta.multiapptabs.gui.custom_views.CustomWebView;
import ar.com.rodrilapenta.multiapptabs.interfaces.ClickListener;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SessionManager sessionManager;
    private Boolean isFullscreenActive = false;
    private CustomWebView wb;
    private AudioManager audioManager;
    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings;
    private LinearLayout layoutFabVolumeUp, layoutFabVolumeDown, layoutFabFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sessionManager = SessionManager.getInstance(MainActivity.this);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        fabSettings = this.findViewById(R.id.fabSetting);

        layoutFabVolumeUp = this.findViewById(R.id.layoutFabVolumeUp);
        layoutFabVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }
        });
        layoutFabVolumeDown = this.findViewById(R.id.layoutFabVolumeDown);
        layoutFabVolumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        });

        layoutFabFullscreen = this.findViewById(R.id.layoutFabFullscreen);
        layoutFabFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullscreenActive) hideSystemUI();
                else showSystemUI();
                isFullscreenActive = !isFullscreenActive;
            }
        });

        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });

        closeSubMenusFab();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        mRecyclerView = findViewById(R.id.recyclerWebInstances);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        wb = findViewById(R.id.webview);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wb.setScaleGestureDetector(new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                double scaleFactor = scaleGestureDetector.getScaleFactor();
                if (scaleFactor > 1.0) hideSystemUI();
                else showSystemUI();
                return true;
            }
        }));
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished (WebView view, String url) {
                view.evaluateJavascript(
                        "(function() { " +
                                "var metas = document.getElementsByTagName('meta'); \n" +
                                "\n" +
                                "   for (var i=0; i<metas.length; i++) { \n" +
                                "      if (metas[i].getAttribute(\"name\") == \"theme-color\") { \n" +
                                "         return metas[i].getAttribute(\"content\"); \n" +
                                "      } \n" +
                                "   }})();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                System.out.println("HTML COLOR: " + html);
                                if(!html.equals("null")) {
                                    toolbar.setBackgroundColor(Color.parseColor(html.replace("\"", "")));
                                    getWindow().setStatusBarColor(Color.parseColor(html.replace("\"", "")));
                                }
                                else {
                                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                                }
                            }
                        });
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                mRecyclerView, new ClickListener() {

            @Override
            public void onClick(View view, final int position) {
                String uri = ((TextView) view.findViewById(R.id.webInstanceUri)).getText().toString();
                toolbar.setTitle(((TextView) view.findViewById(R.id.webInstanceName)).getText().toString());
                wb.loadUrl(uri);
            }

            @Override
            public void onLongClick(View view, int position) {
                String firebaseId = ((TextView) view.findViewById(R.id.webInstanceFirebaseId)).getText().toString();
                sessionManager.deleteWebInstanceByFireabseId(firebaseId);
            }

        }));

        fillGoogleDataOnNavHeader(navigationView);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                loadWebInstances(wb);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                loadWebInstances(wb);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                loadWebInstances(wb);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                loadWebInstances(wb);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadWebInstances(wb);
            }
        };
        sessionManager.getUserFirebaseDatabaseStructure().addChildEventListener(childEventListener);

        loadWebInstances(wb);
    }

    private void closeSubMenusFab(){
        ((FloatingActionButton)layoutFabVolumeUp.findViewById(R.id.fabVolumeUp)).hide();
        ((FloatingActionButton)layoutFabVolumeDown.findViewById(R.id.fabVolumeDown)).hide();
        ((FloatingActionButton)layoutFabFullscreen.findViewById(R.id.fabFullscreen)).hide();
        layoutFabVolumeUp.setVisibility(View.INVISIBLE);
        layoutFabVolumeDown.setVisibility(View.INVISIBLE);
        layoutFabFullscreen.setVisibility(View.INVISIBLE);
        fabSettings.setImageResource(R.drawable.settings);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabVolumeUp.setVisibility(View.VISIBLE);
        layoutFabVolumeDown.setVisibility(View.VISIBLE);
        layoutFabFullscreen.setVisibility(View.VISIBLE);
        ((FloatingActionButton)layoutFabVolumeUp.findViewById(R.id.fabVolumeUp)).show();
        ((FloatingActionButton)layoutFabVolumeDown.findViewById(R.id.fabVolumeDown)).show();
        ((FloatingActionButton)layoutFabFullscreen.findViewById(R.id.fabFullscreen)).show();
        //Change settings icon to 'X' icon
        fabSettings.setImageResource(R.drawable.settings);
        fabExpanded = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addWebInstance:
                final ViewGroup popupView = (ViewGroup) ((LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.dialog_add_webinstance, null);
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Nuevo sitio")
                                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // capturar y guardar en bd
                                        final String NAME = (((TextView) popupView.findViewById(R.id.newWebInstanceName)).getText().toString());
                                        final String DESCRIPTION = (((TextView) popupView.findViewById(R.id.newWebInstanceDescription)).getText().toString());
                                        final String URI = (((TextView) popupView.findViewById(R.id.newWebInstanceUri)).getText().toString());
                                        if (NAME.isEmpty() || DESCRIPTION.isEmpty() || URI.isEmpty()) {
                                            Toast.makeText(MainActivity.this, "Datos insuficientes", Toast.LENGTH_SHORT).show();

                                        } else {
                                            WebInstance wi = new WebInstance(NAME, DESCRIPTION, URI);
                                            sessionManager.addWebInstance(wi);
                                            Toast.makeText(MainActivity.this, "Sitio agregado", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                alertDialogBuilder.setView(popupView);
                alertDialogBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadWebInstances(final WebView wb) {
        Query query = sessionManager.getUserFirebaseDatabaseStructure();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WebInstance> webInstances = new ArrayList<WebInstance>();
                for (DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> map = (Map<String, String>) addressSnapshot.getValue();
                    webInstances.add(new WebInstance(map.get("name"), map.get("description"), map.get("uri"), map.get("firebaseId")));
                }

                mAdapter = new WebInstancesListAdapter(webInstances, wb);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("FALLO QUERY");
            }
        });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().show();
    }

    public void fillGoogleDataOnNavHeader(NavigationView navigationView) {
        Map<String, String> userData = (Map<String, String>) sessionManager.getLoggedUserData();
        View headerLayout = navigationView.getHeaderView(0);
        ((TextView) headerLayout.findViewById(R.id.googleUserEmail)).setText(userData.get("userGoogleEmail"));
        ((TextView) headerLayout.findViewById(R.id.googleUserName)).setText(userData.get("userGoogleName"));
        new ImageLoadTask(userData.get("userGooglePhotoUrl"), (ImageView) headerLayout.findViewById(R.id.googleUserImage), true).execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                return true;
            case (MotionEvent.ACTION_MOVE):
                return true;
            case (MotionEvent.ACTION_UP):
                return true;
            case (MotionEvent.ACTION_CANCEL):
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode,final KeyEvent event) {
        FloatingActionButton fab = findViewById(R.id.fabSetting);
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                fab.hide();
                closeSubMenusFab();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                fab.show();
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN && !event.isLongPress()) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.show();
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_DOWN && !event.isLongPress()) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.hide();
            return true;
        }
        else {
            return super.dispatchKeyEvent(event);
        }
    }*/
}