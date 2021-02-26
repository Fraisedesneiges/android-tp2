package com.example.matthieugedeon.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListView list = (ListView)findViewById(R.id.list);
        MyAdapter adapter = new MyAdapter(this,new Vector<String>());
        list.setAdapter(adapter);

        AsyncFlickrJSONDataForList fetcher = new AsyncFlickrJSONDataForList(adapter);
        fetcher.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");


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

    class Item{
        String url;

        public Item(String url){
            this.url = url;
        }
    }

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

    class MyAdapter extends BaseAdapter{
        private Context context; //context
        private Vector<String> vector;

        public MyAdapter(Context context, Vector<String> vector){
            this.context = context;
            this.vector = vector;
        }

        @Override
        public int getCount() {
            return vector.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return vector.get(position); //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            if (convertView == null) {
                //convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
                convertView = LayoutInflater.from(context).inflate(R.layout.bitmap_layout, parent, false);
            }

            // get current item to be displayed
            String currentItem = (String) getItem(position);

            // get the TextView for item name and item description
            //TextView textViewItemName = (TextView) convertView.findViewById(R.id.url);

            ImageView image = (ImageView) convertView.findViewById(R.id.image_item);

            // Get a RequestQueue
            RequestQueue queue = MySingleton.getInstance(context.getApplicationContext()).
                    getRequestQueue();

            Response.Listener<Bitmap> rep_listener = response -> {
                Bitmap mp;
                try {
                    mp = getResizedBitmap(response,50,50);
                    image.setImageBitmap(mp);
                }
                catch (Exception e){Log.i("Pute","lel t nul");}
            };

            ImageRequest request = new ImageRequest(
                    currentItem, rep_listener
                    , 50,
                    50, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }});

            //sets the text for item name and item description from the current item object
            //textViewItemName.setText(currentItem);

            // Access the RequestQueue through your singleton class.
            MySingleton.getInstance(context).addToRequestQueue(request);

            // returns the view for the current row
            return convertView;
        }
        public void add(String s){
            vector.add(s);
        }
    }


    class AsyncFlickrJSONDataForList extends AsyncTask<String, Void, JSONObject> {

        JSONObject data;
        MyAdapter adapter;
        AsyncFlickrJSONDataForList(MyAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            JSONArray array;
            try {
                array = data.getJSONArray("items");
                Log.i("NTM",array.toString());
            }
            catch (Exception e){
                array = new JSONArray();
            }

            String s;
            try {

                for(int i = 0; i < array.length();i++){
                    s = array.getJSONObject(i).getJSONObject("media").getString("m");
                    adapter.add(s);
                    Log.i("Adding url to adapter", s);
                    adapter.notifyDataSetChanged();
                }
            }
            catch (Exception e){

            }

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

    static class MySingleton {
        private static MySingleton instance;
        private RequestQueue requestQueue;
        private ImageLoader imageLoader;
        private static Context ctx;

        private MySingleton(Context context) {
            ctx = context;
            requestQueue = getRequestQueue();

            imageLoader = new ImageLoader(requestQueue,
                    new ImageLoader.ImageCache() {
                        private final LruCache<String, Bitmap>
                                cache = new LruCache<String, Bitmap>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    });
        }

        public static synchronized MySingleton getInstance(Context context) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            }
            return requestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }

        public ImageLoader getImageLoader() {
            return imageLoader;
        }
    }
}