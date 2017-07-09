package com.example.android.shopping;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class SignIn extends AppCompatActivity {
    private static final String LOG_TAG = "SignIn";
    private Button bt_signInTakePicture;
    private TextureView tx_signInTexture;
    private ImageView iv_test;
    private CameraDevice cameraDevice;
    private String cameraId;
    private Size imageSize;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private Handler handler;
    private HandlerThread handlerThread;
    TextureView.SurfaceTextureListener textureListner;
    private File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        tx_signInTexture = (TextureView) findViewById(R.id.tx_signInTexture);
        bt_signInTakePicture = (Button) findViewById(R.id.bt_signintakepicture);
        iv_test = (ImageView) findViewById(R.id.iv_test);
        bt_signInTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        textureListner = new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
    }
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.d(LOG_TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(SignIn.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    protected void startBackgroundThread() {
        Log.d(LOG_TAG, "startBackgroundThread");
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }
    protected void closeBackgroundThread(){
        handlerThread.quitSafely();
    }
    protected void openCamera(){
        Log.d(LOG_TAG, "openCamera");
        CameraManager cameraManager = (CameraManager) getSystemService(this.CAMERA_SERVICE);
        try{
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageSize =streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SignIn.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            }
            cameraManager.openCamera(cameraId, stateCallback, null);
        }catch (Exception e){
            Log.d(LOG_TAG, e.toString());
        }

    }
    protected void closeCamera(){
        if (cameraDevice !=null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    protected void createCameraPreview() {
        Log.d(LOG_TAG, "createCameraPreview");
        try {
            SurfaceTexture surfaceTexture = tx_signInTexture.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(imageSize.getWidth(), imageSize.getHeight());
            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.d(LOG_TAG, "cameraPreview configured properly");
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(LOG_TAG, "cameraPreview configure failed");
                }
            }, handler);
        }
        catch (Exception e){
            Log.d(LOG_TAG, e.toString());
        }
    }
    protected void updatePreview(){
        Log.d(LOG_TAG, "updatePreview");
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try{
            Log.d(LOG_TAG, "camerCaptureSession null?  " + Boolean.toString(cameraCaptureSession == null));
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
        }catch (Exception e){
            Log.d(LOG_TAG, e.toString());
        }
    }
    protected void takePicture(){
        Log.d(LOG_TAG, "takePicture");
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] sizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            int width = sizes[0].getWidth();
            int height = sizes[0].getHeight();
            ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> surfaces = new ArrayList<Surface>(2);
            surfaces.add(imageReader.getSurface());
            //surfaces.add(new Surface(tx_signInTexture.getSurfaceTexture()));
            Log.d(LOG_TAG, "Surface 0 is valid?  " + Boolean.toString(surfaces.get(0).isValid()));
            //Log.d(LOG_TAG, "Surface 1 is valid?  " + Boolean.toString(surfaces.get(1).isValid()));
            Log.d(LOG_TAG, "available capture request keys:  " + cameraCharacteristics.getAvailableCaptureRequestKeys().toString());
            Log.d(LOG_TAG, "width, height" + Integer.toString(width) + Integer.toString(height));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            file = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
            final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d(LOG_TAG, "onIAL");
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[byteBuffer.capacity()];
                        byteBuffer.get(bytes);
                        save(bytes);
                        getCroppedImage();
                    }catch (Exception e){
                        Log.d(LOG_TAG, e.toString());
                    }finally {
                        if (image != null){
                            image.close();
                        }
                    }

                }
            };
            imageReader.setOnImageAvailableListener(onImageAvailableListener, handler);
            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d(LOG_TAG, "capturecompleted");
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        Log.d(LOG_TAG,"takePicture configured");
                        session.capture(captureBuilder.build(), captureCallback, handler);
                        Log.d(LOG_TAG, "after ccs in onConfigured");
                    }catch (Exception e){
                        Log.d(LOG_TAG, e.toString());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(LOG_TAG, "takePicture Configuration failed");
                }
            }, handler);
        }catch (Exception e){
            Log.d(LOG_TAG, e.toString());
            Log.d(LOG_TAG, "this catch right HERE");
            e.printStackTrace();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        startBackgroundThread();
        if (tx_signInTexture.isAvailable()) {
            openCamera();
        } else {
            tx_signInTexture.setSurfaceTextureListener(textureListner);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeBackgroundThread();
    }
    private void save(byte[] bytes) throws IOException{
        Log.d(LOG_TAG, "save running");
        OutputStream outputStream = null;
        outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        if (outputStream == null){
            outputStream.close();
        }
    }
    private  void getCroppedImage(){
        Log.d(LOG_TAG, "getCroppedImage");
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
        int bHeight = bitmap.getHeight();
        int bWidth = bitmap.getWidth();
        Bitmap croppedBitmap = bitmap.createBitmap(bitmap, 0, 0, bWidth/2, bHeight/5);
        iv_test.setImageBitmap(croppedBitmap);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/cropped.jpg");
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}