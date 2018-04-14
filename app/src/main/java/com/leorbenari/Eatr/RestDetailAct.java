package com.leorbenari.Eatr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yelp.clientlib.entities.Business;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestDetailAct extends AppCompatActivity {

    Business business;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_detail);
        Intent intent = getIntent();
        business = (Business) intent.getExtras().getSerializable("Business");
        Log.v("Business",business.toString());
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutRest);
        TextView textView = (TextView)findViewById(R.id.Detail_Title);
        textView.setText(business.name());

        if(business.isClosed()) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.Red));
            textView = new TextView(getBaseContext());
            textView.setTextSize(35);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setText("Permanently Closed");
            linearLayout.addView(textView);
        }
        textView = (TextView) findViewById(R.id.Detail_id);
        textView.setText(business.id());

        textView = (TextView) findViewById(R.id.Detail_phone);
        textView.setText(business.displayPhone());

        textView = (TextView)findViewById(R.id.Detail_Menu);
        textView.setText(business.menuProvider());

        textView = (TextView) findViewById(R.id.Detail_rating);
        textView.setText(business.rating().toString());

        textView = (TextView) findViewById(R.id.Detail_review);
        textView.setText(business.snippetText());

        textView = (TextView)findViewById(R.id.Detail_Address);
        textView.setText(business.location().displayAddress().toString());

        textView = (TextView)findViewById(R.id.Detail_Distance);
        textView.setText((float)((double)business.distance())+"mtrs");

        if(business.eat24Url()!=null)
        Log.v("WOW","hi" + business.eat24Url().toString());
        else
            Log.v("NO","no");

    }
}
