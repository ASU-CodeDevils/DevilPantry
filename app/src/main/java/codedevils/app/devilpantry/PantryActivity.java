package codedevils.app.devilpantry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        setContentView(R.layout.activity_pantry_add_item);
        Button btn = (Button)findViewById(R.id.button2);

    }

    public void processClick(View v){
        TextView tv = (TextView)findViewById(R.id.txtContent);
        ImageView myImageView = (ImageView)findViewById(R.id.imgview);
        Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.doritos_test);
        myImageView.setImageBitmap(myBitmap);
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();
        if(!detector.isOperational()){
            tv.setText("Could not set up detector!  Try again.");
            return;
        }
        Frame f = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(f);
        Barcode thisCode = barcodes.valueAt(0);
        tv.setText(thisCode.rawValue); //THE TEXTVIEW SHOWS THE UPC NUMBER
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
