package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.graphics.BitmapFactory.decodeFile;
import static android.graphics.BitmapFactory.decodeStream;

public class MainActivity extends BaseApplication {

    ///declaring variables
    private ImageView mImageView;
    private TextView mResult;
    private Button mBtnCamera;
    private Button mBtnGallery;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);
        mResult = (TextView) findViewById(R.id.text_view);
        mBtnCamera = (Button) findViewById(R.id.btn_camera);
        mBtnGallery = (Button) findViewById(R.id.btn_gallery);

        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission(RC_STORAGE_PERMS2);
            }
        });

        mBtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission(RC_STORAGE_PERMS1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                case RC_STORAGE_PERMS2:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = getPath(this, dataUri);
                    if (path == null) {
                        bitmap = resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        bitmap = resizeImage(imageFile, path, mImageView);
                    }
                    if (bitmap != null) {
                        mResult.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        detectFaces(bitmap);
                    }
                    break;
                case RC_TAKE_PICTURE:
                    bitmap = resizeImage(imageFile, imageFile.getPath(), mImageView);
                    if (bitmap != null) {
                        mResult.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        detectFaces(bitmap);
                    }
                    break;
            }
        }
    }

    private void detectFaces(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                mResult.setText(getInfoFromFaces(faces));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mResult.setText(R.string.error);
            }
        });
    }

    private String getInfoFromFaces(List<FirebaseVisionFace> faces) {
        StringBuilder result = new StringBuilder();
        float smileProb = 0;
        float leftEyeOpenProb = 0;
        float rightEyeOpenProb = 0;
        for (FirebaseVisionFace face : faces) {

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and nose available):

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                smileProb = face.getSmilingProbability();
            }
            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProb = face.getLeftEyeOpenProbability();
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProb = face.getRightEyeOpenProbability();
            }

            result.append("Smile: ");
            if (smileProb > 0.5) {
                result.append("Yes");
            } else {
                result.append("No");
            }
            result.append("\nLeft eye: ");
            if (leftEyeOpenProb > 0.5) {
                result.append("Open");
            } else {
                result.append("Close");
            }
            result.append("\nRight eye: ");
            if (rightEyeOpenProb > 0.5) {
                result.append("Open");
            } else {
                result.append("Close");
            }
            result.append("\n\n");
        }
        return result.toString();
    }

    public static String getPath(Context context, Uri uri) {
        String path = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    public static Bitmap resizeImage(File imageFile, Context context, Uri uri, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int photoW = options.outWidth;
            int photoH = options.outHeight;

            options.inSampleSize = Math.min(photoW / view.getWidth(), photoH / view.getHeight());
            return compressImage(imageFile, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap resizeImage(File imageFile, String path, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(path, options);

        int photoW = options.outWidth;
        int photoH = options.outHeight;

        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.min(photoW / view.getWidth(), photoH / view.getHeight());
        return compressImage(imageFile, BitmapFactory.decodeFile(path, options));
    }

    private static Bitmap compressImage(File imageFile, Bitmap bmp) {
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }
}
