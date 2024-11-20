package test.camerax;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private boolean defCamera;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private Preview preview;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private ImageButton capture, switchCamera;
    private ImageView imageHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);
        imageHolder = findViewById(R.id.imageView);
        capture = findViewById(R.id.camera_btn);
        switchCamera = findViewById(R.id.switch_camera_btn);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        defCamera = true;

        startCameraX();

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defCamera = !defCamera;
                startCameraX();
            }
        });

    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        //Selector Use case
        if (defCamera) {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        } else {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        }

        //Preview Use case
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //ImageCapture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }


    private void capturePhoto() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

//        String name = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
//        }
//
//// Create output options object which contains file + metadata
//        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
//                .Builder(this.getContentResolver(),
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues).build();


        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Uri picUri = outputFileResults.getSavedUri();
                        if(picUri == null){
                            Toast.makeText(CameraActivity.this, "Uri is Empty, Image might be saved", Toast.LENGTH_SHORT).show();
                        }else {
                            imageHolder.setImageURI(picUri);
//                            Glide.with(getBaseContext())
//                                    .load(picUri)
//                                    .into(imageHolder);
                            Toast.makeText(CameraActivity.this, "Image saved at " + picUri.getPath() + " Uri is not Empty, saved", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CameraActivity.this, "Image Capture error", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

    }

    public String getBatchDirectoryName() {
        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/CameraX";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {
        }

        return app_folder_path;
    }

    private void startCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
}