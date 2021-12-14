package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by jhansi on 04/04/15.
 */
public class PickImageFragment extends Fragment {

    private View view;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private Uri fileUri;
    private IScanner scanner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach( activity );
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException( "Activity must implement IScanner" );
        }
        this.scanner = ( IScanner ) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.pick_image_fragment, null );
        init();
        return view;
    }

    private void init() {
        cameraButton = view.findViewById( R.id.cameraButton );
        cameraButton.setOnClickListener( new CameraButtonClickListener() );
        galleryButton = view.findViewById( R.id.selectButton );
        galleryButton.setOnClickListener( new GalleryClickListener() );
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        } else {
            getActivity().finish();
        }
    }

    private void clearTempImages() {
        try {
            File tempFolder = new File( ScanConstants.IMAGE_PATH );
            for (File f : tempFolder.listFiles())
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntentPreference() {
        int preference = getIntentPreference();
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    private boolean isIntentPreferenceSet() {
        int preference = getArguments().getInt( ScanConstants.OPEN_INTENT_PREFERENCE, 0 );
        return preference != 0;
    }

    private int getIntentPreference() {
        int preference = getArguments().getInt( ScanConstants.OPEN_INTENT_PREFERENCE, 0 );
        return preference;
    }


    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    }

    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openMediaContent();
        }
    }

    public void openMediaContent() {
        Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
        intent.addCategory( Intent.CATEGORY_OPENABLE );
        intent.setType( "image/*" );
        startActivityForResult( intent, ScanConstants.PICKFILE_REQUEST_CODE );
    }

    public void openCamera() {
        Intent cameraIntent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        File file = createImageFile();
        boolean isDirectoryCreated = file.getParentFile().mkdirs();
        Log.d( "", "openCamera: isDirectoryCreated: " + isDirectoryCreated );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri tempFileUri = FileProvider.getUriForFile( getActivity().getApplicationContext(),
                    "com.aloke.scanlibrarys.provider", // As defined in Manifest
                    file );
            //Uri tempFileUri = Uri.fromFile(file);
            cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, tempFileUri );
        } else {
            Uri tempFileUri = Uri.fromFile( file );
            cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, tempFileUri );
        }
        startActivityForResult( cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE );
    }

    private File createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );

        File basePath = new File( ScanConstants.IMAGE_PATH );
        if (!basePath.exists()) {
            basePath.mkdirs();
        }

        File file = new File( ScanConstants.IMAGE_PATH, "IMG_" + timeStamp + ".jpg" );
        fileUri = Uri.fromFile( file );
        return file;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( "", "onActivityResult" + resultCode );
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = getBitmap( fileUri );
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                        bitmap = getBitmap( data.getData() );
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getActivity().finish();
        }
        if (bitmap != null) {
            postImagePick( bitmap );
        }
    }

    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri( getActivity(), bitmap );
        bitmap.recycle();
        scanner.onBitmapSelect( uri );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Bitmap getBitmap(Uri selectedimg) throws IOException {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( getContext() );
        String imageQuality = sharedPref.getString( "IMAGE_QUALITY", "FAST" ); // "FAST", "HIGH", "HIGHEST"

        if ("HIGHEST".equals( imageQuality )) {
            return BitmapFactory.decodeFile( selectedimg.getPath() );

        } else if ("BETTER".equals( imageQuality )) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            ContentResolver resolver = getActivity().getContentResolver();
            AssetFileDescriptor fileDescriptor = resolver.openAssetFileDescriptor( selectedimg, "r" );
            return BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.getFileDescriptor(), null, options
            );

        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            ContentResolver resolver = getActivity().getContentResolver();
            AssetFileDescriptor fileDescriptor = resolver.openAssetFileDescriptor( selectedimg, "r" );
            return BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.getFileDescriptor(), null, options
            );
        }
    }
}