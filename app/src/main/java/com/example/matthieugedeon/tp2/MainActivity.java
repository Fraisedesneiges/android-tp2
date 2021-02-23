package com.example.matthieugedeon.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

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

    class parameterizedRunnable{
        boolean res;
        Runnable runnable;
        parameterizedRunnable(boolean res){
            this.res = res;
            this.runnable = new Runnable() {
                @Override
                public void run() {
                    TextView result = (TextView) findViewById(R.id.auth_result);
                    result.setText("Connected: " + res);
                }
            };
        }

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

                    JSONObject result;
                    try{
                        result = new JSONObject(s);
                    }
                    catch(Exception e){
                        result = new JSONObject();
                    }

                    parameterizedRunnable pr;
                    try {
                        pr = new parameterizedRunnable(result.getBoolean("authenticated"));
                    }
                    catch (Exception e){
                        pr = new parameterizedRunnable(false);
                    }

                    runOnUiThread(pr.runnable);
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