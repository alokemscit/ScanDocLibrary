package com.aloke.scanlibrarys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;

public class MainScanActivity extends AppCompatActivity {
    private ImageView img;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_scan );
        img = findViewById( R.id.imageView );
        findViewById( R.id.button ).setOnClickListener( v -> {


            Intent intent = new Intent( this, ScanActivity.class );
            intent.putExtra( ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA );

            //startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation( this );
            startActivityForResult( intent, ScanConstants.START_CAMERA_REQUEST_CODE, options.toBundle() );


        } );


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if ((requestCode == ScanConstants.PICKFILE_REQUEST_CODE || requestCode == ScanConstants.START_CAMERA_REQUEST_CODE) &&
                resultCode == Activity.RESULT_OK) {


            //  boolean saveMode = data.getExtras().containsKey(ScanConstants.SAVE_PDF) ? data.getExtras().getBoolean( ScanConstants.SAVE_PDF ) : Boolean.FALSE;
            //if(saveMode){
            // savePdf();

            //  } else {
            Uri uri = data.getExtras().getParcelable( ScanConstants.SCANNED_RESULT );

            final File sd = Environment.getExternalStorageDirectory();
            File src = new File( sd, uri.getPath() );
            /*if (src.exists()) {
                if (src.delete()) {
                    System.out.println("file Deleted :" + uri.getPath());
                } else {
                    System.out.println("file not Deleted :" + uri.getPath());
                }
            }*/


            //  boolean doScanMore = data.getExtras().getBoolean(ScanConstants.SCAN_MORE);


            Bitmap bitmap = BitmapFactory.decodeFile( src.getAbsolutePath() );

            img.setImageBitmap( bitmap );

            try {
                File tempFolder = new File( ScanConstants.IMAGE_PATH );
                for (File f : tempFolder.listFiles())
                    f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }


            //   int k=   getContentResolver().delete(uri, null, null);


            /*File fdelete = new File(data.getData().getPath(),null);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.d("delete","deleted");
                } else {
                    Log.d("delete","not deleted");
                }
            }*/


            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // saveBitmap(bitmap, doScanMore);

            //  if (doScanMore) {
            // scannedBitmaps.add(uri);
            // Intent intent = new Intent(this, MultiPageActivity.class);
            // intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

            // startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
            //  }

            //getContentResolver().delete(uri, null, null);
            // }
        }
    }

}