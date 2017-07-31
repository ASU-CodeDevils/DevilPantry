package codedevils.app.devilpantry;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.Detector;

import java.io.IOException;

/**
 * Created by Stephanie on 7/28/17.
 */

public class AddItemActivity extends AppCompatActivity{


    private CameraSource cameraSource;
    private SurfaceView cameraPreview;
    private TextView barcodeInfo;
    private static final int RequestCameraPermissionID = 1001;

    public AddItemActivity() {}

    public AddItemActivity(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

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
}


