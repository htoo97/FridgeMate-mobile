package com.example.yangliu.fridgemate.current_contents;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
        params.put("i", "ham,cheese");
        RequestHandle data = client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                // Handle resulting parsed JSON response here

                JSONArray data = null;
                try {
                    data = response.getJSONArray("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (data != null) {
                    try {
                        //Testing success
                        TextView recipeText = (TextView) findViewById(R.id.recipeResponse);
                        recipeText.setMovementMethod(new ScrollingMovementMethod());
                        recipeText.setText("Recipes with your ingredients: " + "\n" );
                        //Loop through recipes, append to textview
                        for (int i=0; i < data.length(); i++) {
                            JSONObject recipe = data.getJSONObject(i);
                            String title1 = recipe.getString("title");
                            String url1 = recipe.getString("href");
                            recipeText.append(title1 + ": " + "\n" + url1 + "\n\n");
                        }

                        //String title1 = data.getJSONObject(0).getString("title");
                        //String url1 = data.getJSONObject(0).getString("href");

                        //String title2 = data.getJSONObject(1).getString("title");
                       //String url2 = data.getJSONObject(1).getString("href");
                        //recipeText.setText(title1 + ": " + url1 + "\n" + title2 + ": " + url2);

                        JSONArray res = response.getJSONArray("Response");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Parse JSON for relevant information
                    //TextView recipeText = (TextView) findViewById(R.id.recipeResponse);
                    //recipeText.setText("Testing!");


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

            //Button to update text
        /*
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.findRecipesButton);
            fab.setOnClickListener(new View.OnClickListener()
            {

                TextView recipeText = (TextView) findViewById(R.id.recipeResponse);
                @Override
                public void onClick (View view){
                recipeText.setText("Testing!");
            }
            });
        */

        };
    }
