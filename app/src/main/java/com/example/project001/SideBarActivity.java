package com.example.project001;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.project001.database.DBConnection;
import com.example.project001.database.Trip;
import com.example.project001.fragment.HomeFragment;
import com.example.project001.fragment.ProfileFragment;
import com.example.project001.fragment.SettingsFragment;
import com.example.project001.fragment.TripFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class SideBarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Variables
    LinearLayout profile;
    String displayName;
    Uri profilePic;
    URL url;
    String Email;
    GoogleSignInClient googleApiClient;
    DrawerLayout drawerLayout;
    public static String email;

    //DB
    DBConnection dbc = new DBConnection();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log In
        setContentView(R.layout.activity_sidebar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = GoogleSignIn.getClient(this, gso);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent1 = getIntent();

        displayName = intent1.getStringExtra("Display");
        email = intent1.getStringExtra("Email");
        profilePic = LoginActivity.personPhoto;

        //checks if the profile exists in the database
        dbc.checkIfExists(email, displayName);

        Bundle bun = new Bundle();
        bun.putString("email", email);

        HomeFragment hom = new HomeFragment();
        hom.setArguments(bun);


        //Set Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, hom).commit();
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


    //Create Side Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);

        //Set profile name
        TextView DisplayNameArea = findViewById(R.id.DisplayName);
        DisplayNameArea.setText(displayName);

        //Handle Profile Clicked
        profile = findViewById(R.id.profile);
        profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, new ProfileFragment()).commit();

                DrawerLayout drawer = findViewById(R.id.drawer_layout);

                Log.e("", "PROFILE CLICKED");
                drawer.closeDrawer(GravityCompat.START);
            }
        });


        //Handle Profile Picture
        try {
            url = new URL(profilePic.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadImageTask((ImageView) findViewById(R.id.ProfilePic)).execute(String.valueOf(url));



        return true;
    }


    //Handle Option Bar selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            System.out.println("info");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Handle Side Menu Buttons
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment fragment = null;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        System.out.println("id: " + id);


        //Handle buttons
        if (id == R.id.nav_messages) {
            System.out.println("messages");
            startActivity(new Intent(SideBarActivity.this, com.example.project001.message.MainActivity.class));

        } else if (id == R.id.nav_trips) {

            Bundle bun = new Bundle();
            bun.putString("email", email);

            TripFragment trp = new TripFragment();
            trp.setArguments(bun);

            getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, trp).commit();

        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, fragment).commit();

        } else if (id == R.id.nav_logout) {
            signOut();

        } else if (id == R.id.nav_home) {

            Bundle bun = new Bundle();
            bun.putString("email", email);

            HomeFragment hom = new HomeFragment();
            hom.setArguments(bun);


            //Set Fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, hom).commit();
            Log.e("curious", "biatch");
        }

        //Close Side Menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Handle Sign Out
    public void signOut() {
        googleApiClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }


    //Convert URL into Bitmap for Profile Picture
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}