
package com.example.my_mini_application;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV;
    private Button snapBtn,detectBtn;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTV=findViewById(R.id.idTVDetectedText);
        snapBtn = findViewById(R.id.idBtnSnap);
        detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                detectText();
            }
        });


        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()){
                    captureImage();
                }
                else
                {
                    requestPermission();
                }
            }
        });
    }
    private boolean checkPermission(){
        int canerPermission= ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);//static constant permission manifest
        return canerPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission(){
        int PERMISSION_CODE=200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERMISSION_CODE);
    }



    private void captureImage(){
        Intent takePicture= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(takePicture,REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission= grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(cameraPermission)
            {
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                captureImage();
            }
            else
            {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override   //check whether permission is granted or not by requestpermission method
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {
            Bundle extras=data.getExtras();
            imageBitmap=(Bitmap) extras.get("data");//captue the image and hold the data in imagebitmap
            captureIV.setImageBitmap(imageBitmap);
        }
    }
    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageBitmap,0);//rotation for i
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks())
                {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();//hold the 2 integer coordinates of text
                    Rect blockFrame = block.getBoundingBox();//Rect holds four integer coordinates for a rectangle. The rectangle is represented by the coordinates of its 4 edges
                    for(Text.Line line : block.getLines()){
                        String lineTExt = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for(Text.Element element : line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);

                        }
                        resultTV.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this,"Fail to detect text try ahgain later"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


}