package ar.com.rodrilapenta.favsites.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
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
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.com.rodrilapenta.favsites.R;
import ar.com.rodrilapenta.favsites.controller.SessionManager;
import ar.com.rodrilapenta.favsites.controller.asynctasks.ImageLoadTask;
import ar.com.rodrilapenta.favsites.controller.listeners.RecyclerTouchListener;
import ar.com.rodrilapenta.favsites.db.model.WebInstance;
import ar.com.rodrilapenta.favsites.gui.adapter.WebInstancesListAdapter;
import ar.com.rodrilapenta.favsites.gui.custom_views.CustomWebView;
import ar.com.rodrilapenta.favsites.gui.custom_views.MovableFloatingActionButton;
import ar.com.rodrilapenta.favsites.interfaces.ClickListener;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;
    private Boolean isFullscreenActive = false;
    private CustomWebView wb;
    private AudioManager audioManager;
    private boolean actionFabsHidden = true, webFabsHidden = true, doubleBackToExitPressedOnce = false;
    private MovableFloatingActionButton fabVolumeUp, fabVolumeDown, fabFullscreen, fabWebBack, fabWebForward;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sessionManager = SessionManager.getInstance(MainActivity.this);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        fabVolumeUp = this.findViewById(R.id.fabVolumeUp);
        fabVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }
        });
        fabVolumeDown = this.findViewById(R.id.fabVolumeDown);
        fabVolumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        });

        fabFullscreen = this.findViewById(R.id.fabFullscreen);
        fabFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullscreenActive) hideSystemUI();
                else showSystemUI();
                isFullscreenActive = !isFullscreenActive;
            }
        });

        fabWebBack = this.findViewById(R.id.fabWebBack);
        fabWebBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wb.goBack();
            }
        });

        fabWebForward = this.findViewById(R.id.fabWebForward);
        fabWebForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wb.goForward();
            }
        });

        hideActionFabs();
        hideWebFabs();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        mRecyclerView = findViewById(R.id.recyclerWebInstances);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        wb = findViewById(R.id.webview);
        wb.setToolbar(toolbar);
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

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                mRecyclerView, new ClickListener() {

            @Override
            public void onClick(View view, final int position) {
                String uri = ((TextView) view.findViewById(R.id.webInstanceUri)).getText().toString();
                toolbar.setTitle("Cargando sitio...");
                drawerLayout.closeDrawer(Gravity.START);
                wb.setTitle(((TextView) view.findViewById(R.id.webInstanceName)).getText().toString());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ViewGroup popupView;
        final AlertDialog.Builder alertDialogBuilder;
        switch (item.getItemId()) {
            case R.id.addWebInstance:
                popupView = (ViewGroup) ((LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.dialog_add_webinstance, null);
                alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Nuevo sitio")
                    .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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
            case R.id.google_logout:
                signOut();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, UserSettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.addThisUrl:
                popupView = (ViewGroup) ((LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.dialog_add_webinstance, null);
                ((TextView) popupView.findViewById(R.id.newWebInstanceUri)).setText(wb.getUrl());
                alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Agregar este sitio")
                            .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
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

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);;
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.logout(getApplicationContext());
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void loadWebInstances(final WebView wb) {
        Query query = sessionManager.getUserFirebaseDatabaseStructure();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WebInstance> webInstances = new ArrayList<>();
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Atrás nuevamente para salir", Toast.LENGTH_SHORT).show();

            new android.os.Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
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

    private void hideActionFabs(){
        fabVolumeUp.hide();
        fabVolumeDown.hide();
        fabFullscreen.hide();
    }

    private void showActionFabs(){
        fabVolumeUp.show();
        fabVolumeDown.show();
        fabFullscreen.show();
    }

    private void hideWebFabs(){
        fabWebBack.hide();
        fabWebForward.hide();
    }

    private void showWebFabs(){
        fabWebBack.show();
        fabWebForward.show();
    }

    @Override
    public boolean onKeyDown(final int keyCode,final KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(webFabsHidden) {
                    showWebFabs();
                }
                else {
                    hideWebFabs();
                }
                webFabsHidden = !webFabsHidden;
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(actionFabsHidden) {
                    showActionFabs();
                }
                else {
                    hideActionFabs();
                }
                actionFabsHidden = !actionFabsHidden;
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }
}