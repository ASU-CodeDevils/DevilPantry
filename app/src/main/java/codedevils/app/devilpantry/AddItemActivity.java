package codedevils.app.devilpantry;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.Detector;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddItemActivity extends AppCompatActivity{
    private MainAppDB db;
    private SQLiteDatabase pantryDB;
    private CameraSource cameraSource;
    private SurfaceView cameraPreview;
    private TextView barcodeInfo;
    private static final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_add_item);

        final BarcodeDetector detector = new BarcodeDetector.Builder(this).build();

        // TextView for the information from the barcode.
        barcodeInfo = (TextView) findViewById((R.id.json_text));
        //Captures a stream of images from the camera.
        cameraSource = new CameraSource
                .Builder(this, detector)
                .setFacing(cameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();


        //The following is the preview area of the rear facing camera.
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Requests permission.
                    ActivityCompat.requestPermissions(AddItemActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            public void release(){
                cameraSource.stop();
            }

            public void receiveDetections(Detector.Detections<Barcode> detections){
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() != 0){
                    barcodeInfo.post(new Runnable() {
                        public void run() {
                            barcodeInfo.setText(    //Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );
                        }
                    });
                }
            }
        });
    }

    private String processUPC(String upc){
        String jsonResult = "PROCESSING...";
        try{
            String tag = "PROCESSING UPC";
            Log.i(tag, upc);
            AddItemActivity.RetrieveJsonInfoTask task = new AddItemActivity.RetrieveJsonInfoTask();
            jsonResult = processJson(task.execute(upc).get());
        }catch (Exception e){
            String tag = "ERROR";
            Log.e(tag, e.getMessage(), e);
            jsonResult = "ERROR RETRIEVING JSON";
        }
        return jsonResult;
    }

    private String processJson(String jsonString){
        String ret = "FAILED";
        try{
            JSONObject obj = new JSONObject(jsonString);
            if(obj.getString("valid").equals("true")){
                String upc = obj.getString("number");
                String item = obj.getString("itemname");
                String description = obj.getString("alias");
                String price = obj.getString("avg_price");

                try{
                    db = new MainAppDB(this);
                    pantryDB = db.openDB();
                    String check = "SELECT Quantity FROM pantry WHERE UPC = '" + upc + "';";
                    Cursor c = pantryDB.rawQuery(check, null);
                    if(!c.moveToFirst()){
                        String insert = "INSERT INTO pantry(UPC, Item, Description, Quantity, Price) VALUES " +
                                "('" + upc + "', '" + item + "', '" + description + "', '" + "1" + "', '" + price + "');";
                        pantryDB.execSQL(insert);
                        ret = "ADDED ITEM TO PANTRY";
                    }
                    else{
                        int newQuant = Integer.parseInt(c.getString(0)) + 1;
                        String update = "UPDATE pantry SET Quantity = '" + newQuant + "' WHERE UPC = '" + upc + "';";
                        pantryDB.execSQL(update);
                        ret = "UPDATED EXISTING QUANTITY";
                    }
                    c.close();
                    pantryDB.close();
                    db.close();
                }
                catch (Exception e){
                    String tag = "ERROR";
                    Log.e(tag, e.getMessage(), e);
                }
            }
            if(obj.getString("valid").equals("false")){
                ret = "FAILED: NOT FOUND IN LOOKUP";
                //setContentView(R.layout.activity_pantry_add_item_manual);
            }
        } catch (Exception e){
            Log.e("ERROR", "not a Json object");
        }
        return ret;
    }

    public void processClick(View v){
        cameraSource.stop();

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