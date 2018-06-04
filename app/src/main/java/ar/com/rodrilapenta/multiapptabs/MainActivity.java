package ar.com.rodrilapenta.multiapptabs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
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

import ar.com.rodrilapenta.multiapptabs.controller.SessionManager;
import ar.com.rodrilapenta.multiapptabs.controller.asynctasks.ImageLoadTask;
import ar.com.rodrilapenta.multiapptabs.db.model.WebInstance;
import ar.com.rodrilapenta.multiapptabs.db.repository.WebInstanceRepository;
import ar.com.rodrilapenta.multiapptabs.gui.adapter.WebInstancesListAdapter;
import ar.com.rodrilapenta.multiapptabs.gui.custom_views.CustomWebView;
import ar.com.rodrilapenta.multiapptabs.interfaces.ClickListener;

public class MainActivity extends AppCompatActivity {
    private WebInstanceRepository webInstanceRepository;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SessionManager sessionManager;
    private Boolean isFullscreenActive = false;
    private CustomWebView wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sessionManager = SessionManager.getInstance(MainActivity.this);
        //sessionManager.getWebInstances();


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullscreenActive) hideSystemUI();
                else showSystemUI();
                isFullscreenActive = !isFullscreenActive;
            }
        });

        //View v = findViewById(R.id.)
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        webInstanceRepository = WebInstanceRepository.getInstance(MainActivity.this);

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
                                // Set a background drawable for the ActionBar
                                // Set a color drawable as ActionBar background
                                // This will change the ActionBar background color
                                if(!html.equals("null")) {
                                    toolbar.setBackgroundColor(Color.parseColor(html.replace("\"", "")));
                                    getWindow().setStatusBarColor(Color.parseColor(html.replace("\"", "")));
                                }
                            }
                        });
            }
        });
        /*wb.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                fab.show();
            }

            @Override
            public void onSwipeRight() {
                fab.hide();
            }
        });*/

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
                Toast.makeText(MainActivity.this, "Long press on position :" + position,
                        Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
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
                // Is better to use a List, because you don't know the size
                // of the iterator returned by dataSnapshot.getChildren() to
                // initialize the array
                final List<Map<String, String>> propertyAddressList = new ArrayList<Map<String, String>>();
                List<WebInstance> webInstances = new ArrayList<>();
                for (DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> map = (Map) addressSnapshot.getValue();
                    webInstances.add(new WebInstance(map.get("name"), map.get("description"), map.get("uri")));
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
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

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
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    FloatingActionButton fab = findViewById(R.id.fab);
                    fab.show();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    FloatingActionButton fab = findViewById(R.id.fab);
                    fab.hide();
                }
                return true;

            case KeyEvent.KEYCODE_BACK:
                if (wb.canGoBack()) {
                    wb.goBack();
                } else {
                    finish();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}

class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    private ClickListener clicklistener;
    private GestureDetector gestureDetector;

    public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

        this.clicklistener = clicklistener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clicklistener != null) {
                    clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
            clicklistener.onClick(child, rv.getChildAdapterPosition(child));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}