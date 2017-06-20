package codedevils.app.devilpantry;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PantryActivity extends AppCompatActivity implements ListView.OnItemClickListener{
    private ListView pantryLV;
    private MainAppDB db;
    private SQLiteDatabase pantryDB;
    private List<HashMap<String, String>> cursorMap;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        Button add = (Button)findViewById(R.id.pantryAdd);
        Button viewAll = (Button)findViewById(R.id.pantryViewAll);
    }

    public void viewAllClicked(View v){
        setContentView(R.layout.activity_pantry_view_all);
        pantryLV = (ListView)findViewById(R.id.pantryView);
        String s = "SELECT brand, item, quantity FROM pantry ORDER BY item ASC;";
        this.populateListView(s);
    }

    public void addClicked(View v){
        //TO DO
    }

    private void populateListView(String select){
        try{
            db = new MainAppDB(this);
            pantryDB = db.openDB();
            String[] colHeaders = this.getResources().getStringArray(R.array.pantry_headers);
            int[] toViewIDs = new int[]{R.id.item_name, R.id.item_description, R.id.item_quantity};
            Cursor c = pantryDB.rawQuery(select, null);
            cursorMap = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> colTitles = new HashMap<>();
            colTitles.put("Name", "Name");
            colTitles.put("Description", "Description");
            colTitles.put("Quantity", "Quantity Available");
            cursorMap.add(colTitles);
            while(c.moveToNext()){
                HashMap<String, String> map = new HashMap<>();
                map.put("Name", c.getString(0));
                map.put("Description", c.getString(1));
                map.put("Quantity", c.getString(2));
                cursorMap.add(map);
            }
            c.close();
            pantryDB.close();
            db.close();
            SimpleAdapter sa = new SimpleAdapter(this, cursorMap, R.layout.list_item, colHeaders, toViewIDs);
            pantryLV.setAdapter(sa);
            pantryLV.setOnItemClickListener(this);
        }
        catch (Exception e){
            e.getMessage();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TO DO

    }

}
