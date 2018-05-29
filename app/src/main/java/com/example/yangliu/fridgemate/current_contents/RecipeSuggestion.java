package com.example.yangliu.fridgemate.current_contents;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import cz.msebera.android.httpclient.Header;

import com.loopj.android.http.*;

import com.example.yangliu.fridgemate.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecipeSuggestion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_suggestion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //API request
        String url = "http://www.recipepuppy.com/api/";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("i", "tomatoes,eggs");
        RequestHandle data = client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                // Handle resulting parsed JSON response here

                JSONArray data = null;
                try {
                    data = response.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (data != null) {
                    try {
                        JSONArray res = response.getJSONArray("Response");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                //Parse JSON for relevant information



                    /*
                    String[] names = new String[data.length()];
                    String[] birthdays = new String[data.length()];
                    for(int i = 0 ; i < data.length() ; i++) {
                        birthdays[i] = data.getString("birthday");
                        names[i] = data.getString("name");
                    }
                    */
                }
            }

                @Override
                public void onFailure( int statusCode, Header[] headers, String res, Throwable t){
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }
            });



            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.findRecipesButton);
            fab.setOnClickListener(new View.OnClickListener()
            {

                EditText recipeText = (EditText) findViewById(R.id.RecipeResponse);
                @Override
                public void onClick (View view){
                recipeText.setText("Testing!");
            }
            });


        };
    }
