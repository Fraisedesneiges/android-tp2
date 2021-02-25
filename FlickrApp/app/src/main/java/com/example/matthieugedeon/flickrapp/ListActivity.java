package com.example.matthieugedeon.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.list_item_layout, parent, false);
            }

            // get current item to be displayed
            String currentItem = (String) getItem(position);

            // get the TextView for item name and item description
            TextView textViewItemName = (TextView)
                    convertView.findViewById(R.id.url);


            //sets the text for item name and item description from the current item object
            textViewItemName.setText(currentItem);


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
}