package codedevils.app.devilpantry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PantryActivity extends AppCompatActivity implements ListView.OnItemClickListener {
    private ListView pantryLV;
    private MainAppDB db;
    private SQLiteDatabase pantryDB;
    private List<HashMap<String, String>> cursorMap;
    private String query;
    TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        Button add = (Button) findViewById(R.id.pantryAdd);
        Button viewAll = (Button) findViewById(R.id.pantryViewAll);
    }

    public void viewAllClicked(View v) {
        setContentView(R.layout.activity_pantry_view_all);
        pantryLV = (ListView) findViewById(R.id.pantryView);
        String s = "SELECT Item, Description, Quantity FROM pantry ORDER BY item ASC;";
        this.populateListView(s);
    }

    public void addClicked(View v) {
        setContentView(R.layout.activity_pantry_add_item);
        Button btn = (Button) findViewById(R.id.process_button);
    }

    public void processClick(View v) {
        tv1 = (TextView) findViewById(R.id.json_text);
        ImageView myImageView = (ImageView) findViewById(R.id.imgview);
        Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.doritos_test);
        myImageView.setImageBitmap(myBitmap);
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();
        if (!detector.isOperational()) {
            tv1.setText(R.string.try_again);
            return;
        }
        Frame f = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(f);
        Barcode thisCode = barcodes.valueAt(0);
        String theCode = thisCode.rawValue;
        //processUPC(theCode);
        tv1.setText(processUPC(theCode));
    }

    private String processUPC(String upc){
        String jsonResult;
        try{
            String tag = "PROCESSING UPC";
            Log.i(tag, upc);
            RetrieveJsonInfoTask task = new RetrieveJsonInfoTask();
            if(processJson(task.execute(upc).get())) {
                jsonResult = "ADDED ITEM TO DATABASE";
            }
            else{
                jsonResult = "COULDN'T ADD ITEM";
            }
        }catch (Exception e){
            String tag = "ERROR";
            Log.e(tag, e.getMessage(), e);
            jsonResult = "ERROR RETRIEVING JSON";
        }
        return jsonResult;
    }

    private boolean processJson(String jsonString){
        boolean ret = false;
        try{
            JSONObject obj = new JSONObject(jsonString);
            if(obj.getString("valid").equals("true")){
                String upc = obj.getString("number");
                String brand = obj.getString("itemname");
                String description = obj.getString("alias");
                String price = obj.getString("avg_price");
                //this should be our sqlite statement:
                String insert = "INSERT INTO pantry(UPC, Item, Description, Quantity, Price) VALUES " +
                        "('" + upc + "', '" + brand + "', '" + description + "', '" + 1 + "', '" + price + "');";
                try{
                    db = new MainAppDB(this);
                    pantryDB = db.openDB();

                    pantryDB.execSQL(insert);

                    pantryDB.close();
                    db.close();
                    ret = true;
                }
                catch (Exception e){
                    String tag = "ERROR";
                    Log.e(tag, e.getMessage(), e);
                }
            }
            if(obj.getString("valid").equals("false")){
                setContentView(R.layout.activity_pantry_add_item_manual);
            }
        } catch (Exception e){
            Log.e("ERROR", "not a Json object");
        }
        return ret;
    }

    private void populateListView(String select) {
        try {
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
            while (c.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("Item", c.getString(0));
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
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TO DO

    }

    private class RetrieveJsonInfoTask extends AsyncTask<String, Void, String> {
        private final String API_URL = "http://api.upcdatabase.org/json/";
        private final String API_KEY = "42ffea5cadc47244a0c0da6ff8ba3e42/0";

        protected String doInBackground(String... params) {
            try {
                String itemCode = params[0];
                URL url = new URL(API_URL + API_KEY + itemCode);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {

                    sb.append(line).append("\n");
                }
                br.close();
                urlConnection.disconnect();

                return sb.toString();
            } catch (Exception e) {
                String tag = "ERROR";
                Log.e(tag, e.getMessage(), e);
                return null;
            }
        }
    }
}