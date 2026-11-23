package com.example.sensoresnav;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1002;

    private ImageView imgCameraPreview;
    private TextView tvPhotoInfo;
    private MaterialButton btnTakePhoto;
    private MaterialButton btnFilterNormal, btnFilterGray, btnFilterSepia, btnFilterInvert;

    private Bitmap originalBitmap = null;
    private Bitmap currentBitmap = null;
    private Uri currentPhotoUri = null;
    private String currentPhotoPath = null;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (currentPhotoPath != null) {
                        File file = new File(currentPhotoPath);
                        if (file.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                            if (bitmap != null) {
                                setOriginalPhoto(bitmap, file.length());
                                return;
                            }
                        }
                    }

                    // Fallback thumbnail if FileProvider extra wasn't retrieved
                    if (result.getData() != null && result.getData().getExtras() != null) {
                        Object data = result.getData().getExtras().get("data");
                        if (data instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) data;
                            setOriginalPhoto(bitmap, 0);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_camera);
        }

        imgCameraPreview = findViewById(R.id.imgCameraPreview);
        tvPhotoInfo = findViewById(R.id.tvPhotoInfo);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        btnFilterNormal = findViewById(R.id.btnFilterNormal);
        btnFilterGray = findViewById(R.id.btnFilterGray);
        btnFilterSepia = findViewById(R.id.btnFilterSepia);
        btnFilterInvert = findViewById(R.id.btnFilterInvert);

        btnTakePhoto.setOnClickListener(v -> checkPermissionAndOpenCamera());

        btnFilterNormal.setOnClickListener(v -> applyFilter(FilterType.NORMAL));
        btnFilterGray.setOnClickListener(v -> applyFilter(FilterType.GRAYSCALE));
        btnFilterSepia.setOnClickListener(v -> applyFilter(FilterType.SEPIA));
        btnFilterInvert.setOnClickListener(v -> applyFilter(FilterType.INVERT));
    }

    private void setOriginalPhoto(Bitmap bitmap, long fileSizeBytes) {
        this.originalBitmap = bitmap;
        this.currentBitmap = bitmap;
        imgCameraPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgCameraPreview.clearColorFilter();
        imgCameraPreview.setImageBitmap(bitmap);

        String sizeText = fileSizeBytes > 0 ? String.format(Locale.getDefault(), " | %.1f KB", fileSizeBytes / 1024.0) : "";
        tvPhotoInfo.setText(String.format(Locale.getDefault(), "Foto: %dx%d px%s [Filtro: Normal]", bitmap.getWidth(), bitmap.getHeight(), sizeText));
    }

    private enum FilterType { NORMAL, GRAYSCALE, SEPIA, INVERT }

    private void applyFilter(FilterType filterType) {
        if (originalBitmap == null) {
            Toast.makeText(this, "Toma una fotografía primero para aplicar filtros", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap filteredBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getConfig());
        Canvas canvas = new Canvas(filteredBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();

        String filterName = "Normal";

        switch (filterType) {
            case GRAYSCALE:
                colorMatrix.setSaturation(0);
                filterName = "Escala de Grises";
                break;
            case SEPIA:
                colorMatrix.setScale(1.0f, 0.95f, 0.82f, 1.0f);
                ColorMatrix sepiaMatrix = new ColorMatrix();
                sepiaMatrix.set(new float[] {
                        0.393f, 0.769f, 0.189f, 0, 0,
                        0.349f, 0.686f, 0.168f, 0, 0,
                        0.272f, 0.534f, 0.131f, 0, 0,
                        0, 0, 0, 1, 0
                });
                colorMatrix.postConcat(sepiaMatrix);
                filterName = "Sepia Vintage";
                break;
            case INVERT:
                colorMatrix.set(new float[] {
                        -1.0f, 0, 0, 0, 255,
                        0, -1.0f, 0, 0, 255,
                        0, 0, -1.0f, 0, 255,
                        0, 0, 0, 1.0f, 0
                });
                filterName = "Negativo / Invertido";
                break;
            case NORMAL:
            default:
                break;
        }

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(originalBitmap, 0, 0, paint);
        currentBitmap = filteredBitmap;
        imgCameraPreview.setImageBitmap(currentBitmap);

        tvPhotoInfo.setText(String.format(Locale.getDefault(), "Foto: %dx%d px [Filtro: %s]", currentBitmap.getWidth(), currentBitmap.getHeight(), filterName));
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.sensoresnav.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            }
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}