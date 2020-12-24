package com.aliismayilov.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<Integer> idArray;
    ArrayList<String> nameArray;
    SQLiteDatabase database;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        idArray = new ArrayList<>();
        nameArray = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArray);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("ID", idArray.get(position));
                intent.putExtra("INFO", "old");
                startActivity(intent);
            }
        });

        getData();
    }


    public void getData(){

        try {
            database = this.openOrCreateDatabase("MyDatabase", MODE_PRIVATE, null);

            Cursor cursor = database.rawQuery("SELECT * FROM photos", null);
            int idIX = cursor.getColumnIndex("ID");
            int nameIX = cursor.getColumnIndex("NAME");

            while ( cursor.moveToNext() ){
                idArray.add(cursor.getInt(idIX));
                nameArray.add(cursor.getString(nameIX));
            }
            cursor.close();
            adapter.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == R.id.add_art_item ){
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("INFO", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}