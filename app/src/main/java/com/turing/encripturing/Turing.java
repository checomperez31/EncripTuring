package com.turing.encripturing;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Turing extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentSonido.OnFragmentInteractionListener, FragmentImagenes.OnFragmentInteractionListener {

    private VideoView reproductor;
    private int posicionVideo;
    private FragmentSonido fragmentSonido;
    private FragmentImagenes fragmentImagenes;
    private View vistaPrincipal;
    private final int MY_PERMISSIONS = 100;
    private final int SELECT_PICTURE = 200;
    private final int SELECT_VIDEO = 201;
    private final int GRABAR_VIDEO = 202;
    public final String TAG = "MECT";

    static {
        if(OpenCVLoader.initDebug()){
            Log.i("OpenCV", "Initialize success");
        }else{
            Log.i("OpenCV", "Initialize failed");
        }
    }

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "Inialize Async success");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vistaPrincipal = (View) findViewById(R.id.layout_principal);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Pedimos Permisos si no se han otorgado
        requierePermiso();



        //Manejo de Fragments

        fragmentSonido = new FragmentSonido();
        fragmentImagenes = new FragmentImagenes();

        getSupportFragmentManager().beginTransaction().add(R.id.FragmentContent, fragmentSonido).commit();


        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallBack);
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
        getMenuInflater().inflate(R.menu.turing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_sonido)
        {
            transaction.replace(R.id.FragmentContent, fragmentSonido, "FRAGMENT_SONIDO");
        }
        else if (id == R.id.nav_imagenes)
        {
            transaction.replace(R.id.FragmentContent, fragmentImagenes, "FRAGMENT_IMAGENES");
        }
        else if (id == R.id.nav_video)
        {

        }

        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Turing.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
            }
        }else{
            showExplanation();
        }
    }

    public boolean requierePermiso()
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) && (shouldShowRequestPermissionRationale(RECORD_AUDIO)) && (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            Snackbar.make(vistaPrincipal , "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);
                }
            }).show();
        }else{
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);
        }

        return false;
    }

    private void showExplanation() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(Turing.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case SELECT_PICTURE:
                    Uri pathSound = data.getData();
                    fragmentSonido.obtenerSonido(pathSound);
                    break;
                case SELECT_VIDEO:
                    Uri pathV = data.getData();
                    fragmentImagenes.obtenerVideo(pathV);
                    break;
                case GRABAR_VIDEO:
                    Uri pathVG = data.getData();
                    fragmentImagenes.obtenerVideo(pathVG);
                    break;
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(fragmentImagenes != null && fragmentImagenes.isVisible()){
            reproductor = fragmentImagenes.getReproductor();
            posicionVideo = reproductor.getCurrentPosition();
            Log.i("POSICIÓN", "Se pausó "+ posicionVideo);
            reproductor.pause();
        }
    }

    @Override
    protected  void onRestart(){
        super.onRestart();
        if(fragmentImagenes != null && fragmentImagenes.isVisible()){
            Log.i("POSICIÓN", "Se reinició " + posicionVideo);
            reproductor.seekTo(posicionVideo);
        }
    }
}
