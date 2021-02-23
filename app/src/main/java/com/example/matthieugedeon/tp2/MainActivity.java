package com.example.matthieugedeon.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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


    }

    public void authenticate(View v){
        authenticateThread t = new authenticateThread();
        t.start();
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

    class authenticateThread extends Thread {
        public void run(){
            URL url = null;
            EditText field1 = (EditText) findViewById(R.id.username_field);
            EditText field2 = (EditText) findViewById(R.id.password_field);

            String authConcat = field1.getText().toString() + ":" + field2.getText().toString();
            try {
                url = new URL("https://httpbin.org/basic-auth/bob/sympa");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                String basicAuth = "Basic " + Base64.encodeToString(authConcat.getBytes(),
                        Base64.NO_WRAP);
                urlConnection.setRequestProperty ("Authorization", basicAuth);

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readStream(in);
                    Log.i("JFL", s);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
    */

}