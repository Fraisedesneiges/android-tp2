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

    //Method that launch the authentication thread
    //Launched when clicking on the button authenticate (linking done in the activity_main.xml file)
    public void authenticate(View v){
        authenticateThread t = new authenticateThread();
        t.start();
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

    //Parameterized Runnable that will be passed to the runOnUIThread to update the view
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

    //Activity logic based in a thread
    class authenticateThread extends Thread {
        public void run(){
            URL url = null;
            EditText field1 = (EditText) findViewById(R.id.username_field);
            EditText field2 = (EditText) findViewById(R.id.password_field);

            //User input credentials concatenated
            String authConcat = field1.getText().toString() + ":" + field2.getText().toString();
            try {

                //Url to the authentication service Bob:sympa
                url = new URL("https://httpbin.org/basic-auth/bob/sympa");

                //HttpURLConnection object supporting HTTP-based basic features
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //Data encoder fo the request
                String basicAuth = "Basic " + Base64.encodeToString(authConcat.getBytes(),
                        Base64.NO_WRAP);

                //Setting the request parameters (key, value)
                urlConnection.setRequestProperty ("Authorization", basicAuth);

                try {
                    //Getting the InputStream from the Url connection in order to get data
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    //Getting the data from the InputStream thanks to the readStream method
                    String s = readStream(in);
                    Log.i("JFL", s);

                    JSONObject result;
                    try{
                        //Constructing a JSONObject from the fetched string
                        result = new JSONObject(s);
                    }
                    catch(Exception e){
                        result = new JSONObject();
                    }

                    parameterizedRunnable pr;
                    try {
                        //Instantiate a parameterizedRunnable for the runOnUIThread
                        pr = new parameterizedRunnable(result.getBoolean("authenticated"));
                    }
                    catch (Exception e){
                        pr = new parameterizedRunnable(false);
                    }

                    //Method to update the view
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

}