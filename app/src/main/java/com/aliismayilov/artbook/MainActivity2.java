package com.aliismayilov.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

public class MainActivity2 extends AppCompatActivity {
    SQLiteDatabase database;
    Bitmap selectedImage;
    ImageView imageView;
    EditText nameText;
    EditText whereText;
    EditText yearText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView= findViewById(R.id.imageView);
        nameText = findViewById(R.id.nameText);
        whereText= findViewById(R.id.whereText);
        yearText = findViewById(R.id.yearText);
        button = findViewById(R.id.button);

        try {
            database = this.openOrCreateDatabase("MyDatabase", MODE_PRIVATE, null);
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String info = intent.getStringExtra("INFO");

        if ( info.matches("new") ){
            nameText.setText("");
            whereText.setText("");
            yearText.setText("");
            button.setVisibility(View.VISIBLE);

            Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.selectimage);
            imageView.setImageBitmap(image);

        }else{
            button.setVisibility(View.INVISIBLE);

            int id = intent.getIntExtra("ID", 0);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM photos WHERE ID = ?", new String[] {String.valueOf(id)});

                int nameIX = cursor.getColumnIndex("NAME");
                int whereIX = cursor.getColumnIndex("WHERE_");
                int yearIX  = cursor.getColumnIndex("YEAR_");
                int image = cursor.getColumnIndex("IMAGE");

                while ( cursor.moveToNext() ){
                    nameText.setText(cursor.getString(nameIX));
                    whereText.setText(cursor.getString(whereIX));
                    yearText.setText(cursor.getString(yearIX));

                    byte[] bytes = cursor.getBlob(image);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void selectImage(View view){
        if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity2.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{
            Intent intentGalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentGalery, 2);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent intentGalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentGalery, 2);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ( requestCode == 2 && resultCode == RESULT_OK && data != null ){
            Uri imageUri = data.getData();

            try{
                if ( Build.VERSION.SDK_INT >= 28 ){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                }else{
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }
            }catch ( IOException e){
                e.printStackTrace();
            }

            imageView.setImageBitmap(selectedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void save(View view){
        System.out.println("SAVE ---------------------------------");
        String imageName = nameText.getText().toString();
        String imageWhere= whereText.getText().toString();
        String imageYear = yearText.getText().toString();

        if ( imageName != null && imageWhere != null && imageYear != null ){

            Bitmap smallImage = makeSmallerImage(selectedImage, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS photos (" +
                            "ID INTEGER PRIMARY KEY, " +
                            "NAME VARCHAR," +
                            "WHERE_ VARCHAR," +
                            "YEAR_ INT," +
                            "IMAGE BLOB)");

            String sqlString = "INSERT INTO photos (NAME, WHERE_, YEAR_, IMAGE) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, imageName);
            sqLiteStatement.bindString(2, imageWhere);
            sqLiteStatement.bindString(3, imageYear);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

            System.out.println("SQl e yazdi !!!!!!!!!!!!!!!");

            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }


    public Bitmap makeSmallerImage( Bitmap image, int maximumSize ){
        int width = image.getWidth();
        int height= image.getHeight();

        float bitmapRatio = width / height;

        if ( bitmapRatio > 1 ){
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }else{
            height = maximumSize;
            width  = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}