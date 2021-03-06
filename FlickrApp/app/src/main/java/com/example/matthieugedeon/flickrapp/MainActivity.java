package com.example.matthieugedeon.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

        //Finding the fetch button by it's id and linking a tailored onClickListener
        Button b1=(Button)findViewById(R.id.fetch_button);
        b1.setOnClickListener(new GetImageOnClickListener());

        //Linking an anonymous onClickListener to the list button
        Button b2 = (Button) findViewById(R.id.list_button);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, concat);
                startActivity(intent);
            }
        });
    }

    //Function found on StackOverflow
    //Construct a string from data extracted of a Stream
    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    //Tailored onClickListener that launch our parameterized AsyncTask
    class GetImageOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.i("Button","Clicked");
            AsyncFlickrJSONData fetcher = new AsyncFlickrJSONData();
            fetcher.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");
        }
    }

    //AsyncTask that has for purpose to fetched a bitmap image from a given URL (in our case a Flickr URL)
    class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //Resizing our Bitmap result from doInBackground and setting it into the image ImageView
            Bitmap mp;
            ImageView img;

            try {
                mp = getResizedBitmap(bitmap,500,500);
                img = (ImageView) findViewById(R.id.image);
                img.setImageBitmap(mp);
            }
            catch (Exception e){Log.i("ERROR","Setting Bitmap to ImageView failed");}
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            Bitmap bm = null;

            //In case of several URL, strings being an array of n elements
            for(int i = 0; i<strings.length;i++){
                try {
                    url = new URL(strings[i]);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();

                    //Receive a bitmap from the stream of the connection
                    bm = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                    bm = null;
                }

            }
            return bm;
        }

        //Found on StackOverflow
        //Resize the Bitmap passed as a paramater
        public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, false);

            return resizedBitmap;
        }
    }

    //AsyncTask to get the JSON object containing several URLs from Flickr
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

            //Calling the bitmap downloader in an AsyncTask to get the image from the Flickr URL fetched on the first AsyncTask
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

                        //Trim the unwanted part of the fetched string
                        s = s.replace("jsonFlickrFeed(","");
                        //remove the remaining ")" of the jsonFlickrFeed(
                        s = s.subSequence(0,s.length()-1).toString();
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
