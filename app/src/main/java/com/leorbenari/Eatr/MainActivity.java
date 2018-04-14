package com.leorbenari.Eatr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Category;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;
import com.yelp.clientlib.exception.exceptions.UnavailableForLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImageView mMainImage;
    TextView mTitle;
    TextView mCostCat;
    ProgressBar mLoading;
    private OkHttpClient mClient;
    final private String iKey = "I_KEY";
    final private String iLastKey = "ILAST_KEY";
    final private String restaurantsKey = "RESTAURANTS_KEY";
    private int i;
    private int iLast;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    boolean waiting = false;
    final private int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 103;
    YelpAPIFactory mApiFactory;
    YelpAPI mYelpApi;
    double mLongitude, mLatitude;
    CoordinateOptions mCoordinate;
    Map<String, String> mParams;
    boolean newSession = false;
    int pageNum = 40;
    List<Business> businesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.leorbenari.Eatr.R.layout.activity_main);

        mMainImage = (ImageView) findViewById(com.leorbenari.Eatr.R.id.mainImage);
        mTitle = (TextView) findViewById(com.leorbenari.Eatr.R.id.restaurantLabel);
        mCostCat = (TextView) findViewById(com.leorbenari.Eatr.R.id.costCatLabel);
        mLoading = (ProgressBar) findViewById(com.leorbenari.Eatr.R.id.loading);

        mClient = new OkHttpClient();

        mParams = new HashMap<>();
        mParams.put("term", "food");
        mParams.put("limit", "40");

        if (savedInstanceState != null) {
            i = savedInstanceState.getInt(iKey);
            iLast = savedInstanceState.getInt(iLastKey);
            mRestaurants = savedInstanceState.getParcelableArrayList(restaurantsKey);
        } else {
            i = 0;
            iLast = i;
            newSession = true;
        }
        Log.v("TestLogOncreate",mLatitude +" "+ mLatitude);
        mMainImage.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                newRestaurant();
            }

            public void onSwipeRight() {
                oldRestaurant();
            }

            @Override
            public void onSwipeTop() {
                sameRestaurantNewPic();
            }

            @Override
            public void onSwipeBottom() {
                sameRestaurantPrevPic();
            }

            @Override
            public void onClick() {if (mRestaurants.size() > i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(mRestaurants.get(i).getMainUrl()));
                    startActivity(intent);
                }
            }
        });
        mApiFactory = new YelpAPIFactory(
                getString(com.leorbenari.Eatr.R.string.consumerKey),
                getString(com.leorbenari.Eatr.R.string.consumerSecret),
                getString(com.leorbenari.Eatr.R.string.token),
                getString(com.leorbenari.Eatr.R.string.tokenSecret));
        mYelpApi = mApiFactory.createAPI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            Log.v("TestLogPerm","No permission ");
        } else {
            Log.v("TestLogPerm","GOtcha");
            initLocation();
            waitForRestaurant(true);
        }

    }

    public void initLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLatitude =  34.24165;
            mLongitude = -118.52887;
            Toast.makeText(getBaseContext(),"Setting Default Loaction",Toast.LENGTH_SHORT).show();
            if (newSession) {
                new FindPictures().execute("0");
            }
        } else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                mLatitude =  34.24165;
                mLongitude = -118.52887;

                mCoordinate = CoordinateOptions.builder()
                        .latitude(mLatitude)
                        .longitude(mLongitude).build();
                Toast.makeText(getBaseContext(),"GPS DISABLED",Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Setting San Francisco as default location", Toast.LENGTH_SHORT).show();
                if (newSession) {
                    new FindPictures().execute("0");
                }

            }
            else if (location != null) {
                mLongitude = location.getLongitude();
                mLatitude = location.getLatitude();
                if (newSession) {
                    Log.v("TestLogINIT","Downloading pictures");
                    new FindPictures().execute("0");
                }
            } else {
                Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                lm.requestSingleUpdate(criteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mCoordinate = CoordinateOptions.builder()
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude()).build();
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                        if (newSession) {
                            Log.v("TesTLogOLC","lat:"+mLatitude+"long:"+mLongitude);
                            new FindPictures().execute("0");
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        Toast.makeText(MainActivity.this, "GPS needed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        //Toast.makeText(MainActivity.this, "GPS needed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        Toast.makeText(MainActivity.this, "GPS needed", Toast.LENGTH_SHORT).show();
                    }
                }, null);
            }
        }
        mCoordinate = CoordinateOptions.builder()
                .latitude(mLatitude)
                .longitude(mLongitude).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Setting San Francisco as default location", Toast.LENGTH_SHORT).show();
                }
                initLocation();
                waitForRestaurant(true);
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    synchronized public void waitForRestaurant(boolean client) {
        if (client) {
            if (mRestaurants.size() > i && mRestaurants.get(i).getPictures().size() > mRestaurants.get(i).getCurrPic()) {
                mLoading.setVisibility(View.INVISIBLE);
                recipeCallback();
            } else {
                loadingScreen();
                waiting = true;
            }
        } else {
            if (waiting) {
                mLoading.setVisibility(View.INVISIBLE);
                waiting = false;
                recipeCallback();
            }
        }
    }

    public void recipeCallback() {
        displayRestaurant(mRestaurants.get(i));
    }

    public void loadingScreen() {
        mMainImage.setImageResource(android.R.color.transparent);
        mCostCat.setText("");
        mTitle.setText("");
        mLoading.setVisibility(View.VISIBLE);
    }

    private void newRestaurant() {
        if (mRestaurants.size() > i) {
            i++;
            waitForRestaurant(true);
            if (i - iLast > 5 && mRestaurants.size() - i < 10) {
                iLast = i;
                new FindPictures().execute(""+pageNum);
                pageNum += 40;
            }
        }
    }

    private void oldRestaurant() {
        if (i > 0) {
            i--;
            waitForRestaurant(true);
        }
    }

    private void sameRestaurantNewPic() {
        Restaurant currRestaurant = mRestaurants.get(i);
        if (currRestaurant.getPictures().size() > currRestaurant.getCurrPic()) {
            currRestaurant.incCurrPic();
            waitForRestaurant(true);
            if (currRestaurant.getCurrPic() - currRestaurant.getiLast() > 5 &&
                    currRestaurant.getPictures().size() - currRestaurant.getCurrPic() < 7) {
                currRestaurant.setiLast(currRestaurant.getCurrPic());
                new MorePictures().execute(i);
            }
        }
    }

    private void sameRestaurantPrevPic() {
        Restaurant currRestaurant = mRestaurants.get(i);
        if (currRestaurant.getCurrPic() > 0) {
            currRestaurant.decCurrPic();
            displayRestaurant(currRestaurant);
        }
    }

    public void displayRestaurant(final Restaurant r) {
        Picasso
                .with(MainActivity.this)
                .load(r.getPictures().get(r.getCurrPic()))
                .into(mMainImage);
        mTitle.setText(r.getName());

        //Chinmay's on click listener
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,RestDetailAct.class);//TODO:Write the RestDetailActivity class
                    for (Business b : businesses) {
                        if(b.name().equals(r.getName())){
                            intent.putExtra("Business",b);
                            break;
                        }
                    }
                    startActivity(intent);
                }
            });
        mCostCat.setText(r.getCostCat());
    }

    class FindPictures extends AsyncTask<String, Restaurant, String> {

        List<Restaurant> restaurants = null;

        @Override
        protected void onProgressUpdate(Restaurant... values) {
            super.onProgressUpdate(values);
            if (values != null) {
                mRestaurants.add(values[0]);
                waitForRestaurant(false);
            } else {
                Toast.makeText(MainActivity.this, "No data available for your location", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String page = strings[0];
            mParams.put("offset", page);
            retrofit2.Call<SearchResponse> call = mYelpApi.search(mCoordinate, mParams);
            retrofit2.Response<SearchResponse> sr = null;
            try {
                sr = call.execute();
            } catch (UnavailableForLocation e) {
                Log.v("wow","Caught shit"+e);
            }
            catch (IOException e) {
                e.printStackTrace();
            }


            if (sr != null) {
                restaurants = new ArrayList<>();
                businesses = sr.body().businesses();
                Collections.shuffle(businesses, new Random(System.nanoTime()));
                for (Business b : businesses) {
                    Restaurant r = new Restaurant(b.name(), b.url());
                    r.setCostCat(b.rating() + " " + categoriesToString(b.categories()));
                    restaurants.add(r);
                    fetchPictures(r, "0", restaurants.size()-1);
                }
            }

            return null;
        }

        private String categoriesToString(List<Category> cats) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cats.size(); i++) {
                sb.append(cats.get(i).name());
                if(i != cats.size()-1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        private void fetchPictures(Restaurant restaurant, String page, final int pos) {
            String url = restaurant.getPicUrl() + "&start=" + page;
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<String> pictures = RestaurantParser.getPictures(response.body().string());
                    Collections.shuffle(pictures, new Random(System.nanoTime()));
                    if (pictures.size() > 0) {
                        Restaurant r = restaurants.get(pos);
                        r.setPictures(pictures);
                        publishProgress(r);
                    }
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(iKey, i);
        outState.putInt(iLastKey, iLast);
        outState.putParcelableArrayList(restaurantsKey, (ArrayList<? extends Parcelable>) mRestaurants);
        super.onSaveInstanceState(outState);
    }

    class MorePictures extends AsyncTask<Integer, List<String>, String> {

        private int pos;

        @Override
        protected void onProgressUpdate(List<String>... values) {
            super.onProgressUpdate(values);
            mRestaurants.get(pos).getPictures().addAll(values[0]);
            waitForRestaurant(false);
        }

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mClient.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            pos = integers[0];
            Restaurant r = mRestaurants.get(pos);
            String url = r.getPicUrl() + "&start=" + r.getPage();
            r.setPage(r.getPage() + 30);
            String body = null;
            try {
                body = run(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (body != null) {
                List<String> pictures = RestaurantParser.getPictures(body);
                Collections.shuffle(pictures, new Random(System.nanoTime()));
                publishProgress(pictures);
            }
            return null;
        }
    }

}
