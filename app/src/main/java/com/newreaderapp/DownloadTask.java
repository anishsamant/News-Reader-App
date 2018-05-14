package com.newreaderapp;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.newreaderapp.MainActivity.articleDB;

/**
 * Created by ANISH on 11-05-2018.
 */

public class DownloadTask extends AsyncTask<String, Void, String[]> {


    @Override
    protected String[] doInBackground(String... strings) {

        URL url=null;
        HttpURLConnection conn=null;
        String result="";

        try {

            //get articleIDs
            url=new URL(strings[0]);
            conn=(HttpURLConnection)url.openConnection();
            InputStream in=conn.getInputStream();
            InputStreamReader rd=new InputStreamReader(in);
            int data=rd.read();
            while(data!=-1)
            {
                char curr=(char) data;
                result+=curr;
                data=rd.read();
            }

            JSONArray jsonArray=new JSONArray(result);

            int numberOfItems=20;
            if(jsonArray.length()<20)
                numberOfItems=jsonArray.length();

            //delete current entries from database
            articleDB.execSQL("DELETE FROM articles");

            Log.i("seq0","initial entries deleted");
            Log.i("seq0",Integer.toString(jsonArray.length()));
            Log.i("seq0",Integer.toString(numberOfItems));




            //fetch the required info
            for(int i=0;i<numberOfItems;i++)
            {
                Log.i("seq1", "fetching article ID");
                String articleId = jsonArray.getString(i);  //stores article IDs

                Log.i("seq1.1", articleId);



                //get articleInfo
                Log.i("seq2", "fetching article info");
                url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                conn = (HttpURLConnection) url.openConnection();
                in = conn.getInputStream();
                rd = new InputStreamReader(in);
                data = rd.read();
                String articleInfo = "";  //stores article Info
                while (data != -1) {
                    char curr = (char) data;
                    articleInfo += curr;
                    data = rd.read();
                }

                JSONObject jsonObject = new JSONObject(articleInfo);

                String articleTitle = null;   //stores article Title
                String articleURL = null;     //stores article URL


                if (!jsonObject.isNull("title") && !jsonObject.isNull("url"))
                {
                    articleTitle = jsonObject.getString("title");
                    articleURL = jsonObject.getString("url");

                    if(articleURL.endsWith(".pdf") || articleURL.endsWith(".pdf/"))
                    {
                        Log.i("seqException","current article terminated and moved to next");
                        numberOfItems+=1;
                        continue;
                    }

                    Log.i("seq2.1", articleTitle);
                    Log.i("seq2.2", articleURL);


                    MainActivity.result[0] = articleId;
                    MainActivity.result[1] = articleTitle;
                    MainActivity.result[2] = articleURL;

                    Log.i("seq4", "storing in DB");
                    MainActivity.storeInDB();
                }
                else
                {
                    Log.i("seqException","current article terminated and moved to next");
                    numberOfItems+=1;
                }
            }

            Log.i("seqFINISHED","all the loading is done");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
        MainActivity.updateListView();
    }
}
