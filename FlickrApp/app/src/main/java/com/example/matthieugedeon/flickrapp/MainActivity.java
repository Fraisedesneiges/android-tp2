package com.example.matthieugedeon.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1=(Button)findViewById(R.id.fetch_button);
        b1.setOnClickListener(new GetImageOnClickListener());
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    class GetImageOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.i("Button","Clicked");
            AsyncFlickrJSONData fetcher = new AsyncFlickrJSONData();
            fetcher.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");
        }
    }

    class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap>{
        Bitmap mp;

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView img = (ImageView) findViewById(R.id.image);
            try {
                img.setImageBitmap(mp);
                img.setMaxWidth(50);
                img.setMaxHeight(50);
            }
            catch (Exception e){Log.i("Pute","lel t nul");}
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            Bitmap bm = null;
            for(int i = 0; i<strings.length;i++){
                try {
                    url = new URL(strings[i]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        bm = BitmapFactory.decodeStream(in);
                        Log.i("Pute","gg");
                    }
                    catch (Exception e){
                        bm = null;
                    }
                    finally {
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bm;
        }
    }

    class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject>{

        JSONObject data;

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            TextView tv =(TextView) findViewById(R.id.display);
            tv.setText(data.toString());
            JSONObject first;
            try {
                first = data.getJSONArray("items").getJSONObject(0);
            }
            catch (Exception e){
                first = new JSONObject();
            }
            tv.setText(first.toString());

            String s;
            try {
                s = first.getJSONObject("media").getString("m");
            }
            catch (Exception e){
                s = "";
            }

            AsyncBitmapDownloader imgDownloader = new AsyncBitmapDownloader();
            imgDownloader.execute(s);

            tv.setText(s);

        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            Log.i("Thread","In Thread");
            URL url;
            for(int i = 0; i<strings.length; i++){
                try {
                    url = new URL(strings[i]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = readStream(in);
                        Log.i("JFL", s);
                        Log.i("Size", String.valueOf(s.length()));

                        s = s.replace("jsonFlickrFeed(","");
                        Log.i("pété", s);
                        s = s.subSequence(0,s.length()-1).toString();
                        Log.i("chié", s);
                        try{
                            data = new JSONObject(s);
                        }
                        catch(Exception e){
                            data = new JSONObject();
                        }
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return data;
        }
    }
}