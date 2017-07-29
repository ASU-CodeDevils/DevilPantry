package codedevils.app.devilpantry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button pantryButton = (Button)findViewById(R.id.pantry_button);
        Button grocButton = (Button)findViewById(R.id.grocery_button);
        Button recipeButton = (Button)findViewById(R.id.recipe_button);
    }

    public void pantryClick(View v){
        Intent pIntent = new Intent(this, PantryActivity.class);
        startActivity(pIntent);
    }

    public void grocClick(View v){
        Intent gIntent = new Intent(this, GroceryActivity.class);
        startActivity(gIntent);
    }

    public void recipeClick(View v){
        Intent rIntent = new Intent(this, RecipeActivity.class);
        startActivity(rIntent);
    }
}
