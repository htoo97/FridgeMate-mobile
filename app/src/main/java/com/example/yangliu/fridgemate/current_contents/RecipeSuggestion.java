package com.example.yangliu.fridgemate.current_contents;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.Header;

import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.loopj.android.http.*;

import com.example.yangliu.fridgemate.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

public class RecipeSuggestion extends TitleWithButtonsActivity {


    private AsyncHttpClient client;
    RecipeSuggestionAdapter recipeListAdapter;
    int page;
    private TextView textView;
    private LayoutAnimationController animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_recipe_suggestion);
        // set up toolbar
        setBackArrow();
        setTitle("Recipe Suggestion");
        textView = findViewById(R.id.textView8);

        //API request
        client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        Intent intent = getIntent();
        String toSearch = intent.getStringExtra("search string");
        params.put("i", toSearch);
        page = 1;

        callAPI(params);

        // set up ocr items
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvNumbers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeListAdapter = new RecipeSuggestionAdapter(this);
        recyclerView.setAdapter(recipeListAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(RecipeSuggestion.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String htmlLink = recipeListAdapter.getLink(position);
                if (!htmlLink.startsWith("http://") && !htmlLink.startsWith("https://"))
                    htmlLink = "http://" + htmlLink;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(htmlLink));
                startActivity(browserIntent);
            }
            @Override
            public void onItemLongClick(View view, int position) {}
        }));

        FloatingActionButton nextPage = findViewById(R.id.goNext);
        FloatingActionButton previousPage = findViewById(R.id.goPrevious);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(RecipeSuggestion.this, R.anim.fall_from_right_layout));
                page++;
                callAPI(params);
            }
        });
        previousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page > 1){
                    recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(RecipeSuggestion.this, R.anim.fall_from_left_layout));
                    page--;
                    callAPI(params);
                }
                else{
                    Toast.makeText(RecipeSuggestion.this, "This is the first page.", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void callAPI(RequestParams params){
        params.put("p", page);
        String url = "http://www.recipepuppy.com/api/";
        client.get(url, params, new JsonHttpResponseHandler() {
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
                        List<RecipeItem> dataArr = new LinkedList<RecipeItem>();

                        //Testing success
                        TextView recipeText = (TextView) findViewById(R.id.recipeResponse);
                        String s = "Recipes with your ingredients: " + "\n";
                        //Loop through recipes, append to textview
                        for (int i=0; i < data.length(); i++) {
                            JSONObject recipe = data.getJSONObject(i);
                            String title1 = recipe.getString("title");
                            String url1 = recipe.getString("href");
                            String thumbnail =  recipe.getString("thumbnail");
                            dataArr.add(new RecipeItem(title1,thumbnail,url1));
                        }

                        // add data to list adapter
                        recipeListAdapter.setItems(dataArr);

                        //String title1 = data.getJSONObject(0).getString("title");
                        //String url1 = data.getJSONObject(0).getString("href");

                        //String title2 = data.getJSONObject(1).getString("title");
                        //String url2 = data.getJSONObject(1).getString("href");
                        //recipeText.setText(title1 + ": " + url1 + "\n" + title2 + ": " + url2);

                        JSONArray res = response.getJSONArray("Response");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (recipeListAdapter.getItemCount() == 0)
                        textView.setText("Sorry. We couldn't find relavant recipe for you at the moment.");
                    else
                        textView.setText("");

                }
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, String res, Throwable t){
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                textView.setText("Server Error");

            }
        });

    }
}
