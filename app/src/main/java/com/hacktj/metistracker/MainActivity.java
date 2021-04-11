package com.hacktj.metistracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hacktj.metistracker.ui.main.SectionsPagerAdapter;
import com.neovisionaries.ws.client.HostnameUnverifiedException;
import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /***********************/

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        /****************************/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_black_mic_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                sendSpeechText(data);
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micButton.setImageResource(R.drawable.ic_black_mic_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(MainActivity.this, "Mic up()", Toast.LENGTH_LONG).show();
                    speechRecognizer.stopListening();
                }
                return false;
            }
        });
    }

    private class NetworkAsyncTask extends AsyncTask<String, Void, String> {
        private WebSocket ws;
        private String responseMessage;
        private Context myContext;

        public NetworkAsyncTask(Context context) {
            myContext = context;
        }

        private void createWebSocket(String text) {
            WebSocketFactory factory = new WebSocketFactory();

            try {
                this.ws = factory.createSocket("ws://477b96d1401c.ngrok.io", 5000);

                final NetworkAsyncTask myself = this;

                ws.addListener(new WebSocketAdapter() {

                    @Override

                    public void onTextMessage(WebSocket websocket, String message) throws Exception {

                        // Received a text message.
                        myself.responseMessage = message;
                        System.out.println(myself.responseMessage);
                        ws.disconnect();
                        System.out.println("i disconnected");
                    }

                });

                connectWebSocket(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void connectWebSocket(String text) {
            if(this.ws == null) {
                System.out.println("no websocket");
                return;
            }

            try
            {

                // Connect to the server and perform an opening handshake.

                // This method blocks until the opening handshake is finished.

                ws.connect();

            }

            catch (OpeningHandshakeException e)

            {

                // A violation against the WebSocket protocol was detected

                // during the opening handshake.
                System.out.println("opening handshake exception");

            }

            catch (HostnameUnverifiedException e)

            {

                // The certificate of the peer does not match the expected hostname.
                System.out.println("hostname unverified exception");

            }

            catch (WebSocketException e)

            {

                // Failed to establish a WebSocket connection.
                System.out.println("websocket exception");
                e.printStackTrace();

            }

            this.ws.sendText(text);
        }

        @Override
        protected String doInBackground(String...strings) {
            createWebSocket(strings[0]);
            while(this.responseMessage == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("done with websocket " + this.responseMessage);
            return responseMessage;
        }

        protected void onPostExecute(String response) {
            // send to app
            Toast.makeText(myContext, " " + response, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(MainActivity.this, "on activity result method called", Toast.LENGTH_LONG).show();
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                editText.setText(
                        Objects.requireNonNull(result).get(0));
            } else {
                Toast.makeText(MainActivity.this, "not working!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSpeechText(ArrayList<String> arrayList) {
        String text = "";
        for(String word: arrayList) {
            text += word + " ";
        }
        NetworkAsyncTask myTask = new NetworkAsyncTask(MainActivity.this);
        myTask.execute(text);
    }

}