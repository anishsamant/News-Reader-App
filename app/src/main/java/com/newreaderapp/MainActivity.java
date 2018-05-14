package com.newreaderapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    static ListView listView;
    static ArrayList<String> titles=new ArrayList<>();
    static ArrayList<String> urls=new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    static SQLiteDatabase articleDB;
    static String[] result=new String[3];
    static LinearLayout progressBarLayout;
    static boolean firstCallToUpdateListView=true;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Closing App!")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);

        listView = (ListView) findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter(this, R.layout.listview_text, titles);
        listView.setAdapter(arrayAdapter);

        progressBarLayout.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("url", urls.get(i));
                startActivity(intent);
            }
        });

        articleDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articleDB.execSQL("CREATE TABLE IF NOT EXISTS articles (Id INTEGER PRIMARY KEY, articleID INTEGER, title VARCHAR, url VARCHAR)");

        updateListView();

        //download the content required
        DownloadTask downloadTask=new DownloadTask();
        try {

            //get articleID, articleTitle, articleContent
            downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //store info in DB
    public static void storeInDB()
    {
        String sql="INSERT INTO articles(articleID, title, url) VALUES(?, ?, ?)";
        SQLiteStatement statement=articleDB.compileStatement(sql);

        for(int i=0;i<result.length;i++)
        {
            statement.bindString(i+1,result[i]);
        }

        statement.execute();
        Log.i("seq4.1","inserted to DB");

    }


    //update the listView
    public static void updateListView() {
        Cursor c = articleDB.rawQuery("SELECT * FROM articles", null);

        int urlIndex = c.getColumnIndex("url");
        int titleIndex = c.getColumnIndex("title");

        Log.i("seq0",Boolean.toString(c.moveToFirst()));
        try{
            if (c.moveToFirst()) {
                titles.clear();
                urls.clear();
                do {
                    titles.add(c.getString(titleIndex));
                    urls.add(c.getString(urlIndex));
                } while (c.moveToNext());

                Log.i("seqDONE","list view updating");
                arrayAdapter.notifyDataSetChanged();
                Log.i("seqDONE","list view updated");

                progressBarLayout.setVisibility(View.GONE);
                Log.i("seqDONE","visibility is off");

            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
