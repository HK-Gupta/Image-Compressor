package com.example.imagecompressor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    private static final int CODE = 90901;
    ImageView img_original, img_compressed;
    TextView txt_original, txt_compressed, txt_quality;
    EditText txt_height, txt_width;
    SeekBar seek_quality;
    Button btn_select, btn_compress;
    File original, compressed;
    private static String filePath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myCompressor");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

        img_original = findViewById(R.id.img_original);
        img_compressed = findViewById(R.id.img_compressed);
        txt_original = findViewById(R.id.txt_original);
        txt_compressed = findViewById(R.id.txt_compressed);
        txt_quality = findViewById(R.id.txt_quality);
        txt_height = findViewById(R.id.txt_height);
        txt_width = findViewById(R.id.txt_width);
        seek_quality = findViewById(R.id.seek_quality);
        btn_select = findViewById(R.id.btn_select);
        btn_compress = findViewById(R.id.btn_compress);

        filePath = path.getAbsolutePath();

        if(!path.exists()) {
            path.mkdirs();
        }
        seek_quality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txt_quality.setText("Quality: " + i);
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, CODE);
            }
        });
        btn_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quality = seek_quality.getProgress();
                int width = Integer.parseInt(txt_width.getText().toString());
                int height = Integer.parseInt(txt_height.getText().toString());

                try {
                    compressed = new Compressor(MainActivity.this)
                            .setMaxWidth(width)
                            .setMaxHeight(height)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filePath)
                            .compressToFile(original);
                    File finalFile = new File(filePath, original.getName());
                    Bitmap finalBitMap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    img_compressed.setImageBitmap(finalBitMap);
                    txt_compressed.setText("Size: "+ Formatter.formatShortFileSize(MainActivity.this, finalFile.length()));
                    Toast.makeText(MainActivity.this, "Compressed Successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error While Compressing", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CODE)  {
            btn_compress.setVisibility(View.VISIBLE);
            final Uri img_uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(img_uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                // Check if the file exists
                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    // File exists; you can proceed with further operations
                    try {
                        final InputStream inputStream = getContentResolver().openInputStream(img_uri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                        img_original.setImageBitmap(selectedImage);
                        original = imageFile;
                        txt_original.setText("Size: " + Formatter.formatShortFileSize(this, original.length()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error opening the selected image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // File doesn't exist; handle this condition as needed
                    Toast.makeText(this, "Selected image does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No Image Is Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }
}