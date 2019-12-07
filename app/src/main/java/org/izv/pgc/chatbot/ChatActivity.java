package org.izv.pgc.chatbot;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.izv.pgc.chatbot.apibot.ChatterBot;
import org.izv.pgc.chatbot.apibot.ChatterBotFactory;
import org.izv.pgc.chatbot.apibot.ChatterBotSession;
import org.izv.pgc.chatbot.apibot.ChatterBotType;

import org.izv.pgc.chatbot.apibot.Utils;
import org.izv.pgc.chatbot.model.Message;
import org.izv.pgc.chatbot.view.ChatViewAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener  {
    private static final String TAG = "TextToSpeechDemo";
    private ConstraintLayout clMain;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextToSpeech mTts;
    private Button btSend;
    private String textoUser;
    private EditText etText;
    private List<Message> messageList;
    private ProgressBar pbThink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ativity_chat);

        initComponents();
        initEvents();



    }

    public static String getShortTime()
    {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void updateRecycler(List<Message> messageList){
        if(messageList.size()>0) {
            ChatViewAdapter myAdapter = new ChatViewAdapter(messageList, new ChatViewAdapter.onItemClickListener() {
                @Override
                public void onItemClick(Message message) {
                    Toast.makeText(ChatActivity.this, "Mensaje " + message.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            recyclerView.setAdapter(myAdapter);
        }
        else{
            TextView tv = new TextView(getApplicationContext());
            tv.setText("No hay Mensajes");
            clMain.addView(tv);

        }
    }

    private void initEvents() {

        // TextToSpeech.OnInitListener
        mTts = new TextToSpeech(this,this);
        pbThink.setVisibility(View.INVISIBLE);

        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pbThink.setVisibility(View.VISIBLE);

                textoUser = etText.getText().toString();
               /* Message message = new Message();
                message.setFrom(false);
                message.setMessage(textoUser);
                message.setTime(getShortTime());
                messageList.add(message);
                updateRecycler(messageList);*/
                //talkMe(textoUser);
                System.out.println("user> " + textoUser);
                new TranslateToEng(textoUser).execute();
                //new Chat(textoUser).execute();

                etText.setText("");
            }
        });

    }

    private void initComponents() {
        clMain = findViewById(R.id.clMain);
        recyclerView = findViewById(R.id.messageList);
        btSend = findViewById(R.id.btSend);
        etText = findViewById(R.id.etText);
        pbThink = findViewById(R.id.progressBar);
    }

    private void talkMe(String text) {
        mTts.speak(text,
                TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
                null);

    }

    private void chat(String userText){
        try{
            ChatterBotFactory factory = new ChatterBotFactory();

            ChatterBot bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            ChatterBotSession bot1session = bot1.createSession();

            String s;

                s = bot1session.think(userText);
                new TranslateToEsp(s).execute();
                //s = decomposeJson(s);
                //talkMe(s);
                //System.out.println("bot1> " + s);

                //tvLog.setText(tvLog.getText().toString() + s);

        }catch (Exception e){

        }
    }

    @Override
    public void onInit(int status) {

        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.getDefault());
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.
                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                btSend.setEnabled(true);
                // Greet the user.
                talkMe("¿En que puedo ayudarle?");
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    public String decomposeJson(String json){
        String translationResult = "Could not get";
        try {
            JSONArray arr = new JSONArray(json);
            JSONObject jObj = arr.getJSONObject(0);
            translationResult = jObj.getString("translations");
            JSONArray arr2 = new JSONArray(translationResult);
            JSONObject jObj2 = arr2.getJSONObject(0);
            translationResult = jObj2.getString("text");
        } catch (JSONException e) {
            translationResult = e.getLocalizedMessage();
        }
        return translationResult;
    }

    private class Chat extends AsyncTask <String,Void,Void> {
        String v;

        public Chat(String s){
            this.v = s;
        }

        @Override
        protected Void doInBackground(String... strings) {
            chat(v);
            return null;
        }
    }

    private class TranslateToEng extends AsyncTask<Void, Void, Void>{
        HashMap<String, String> httpBodyParams;
        private String parameters, s;

        public TranslateToEng(String message) {
            httpBodyParams = new HashMap<>();
            httpBodyParams.put("fromLang", "es");
            httpBodyParams.put("to", "en");
            httpBodyParams.put("text", message);

            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : httpBodyParams.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");
                try {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.append("=");
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            parameters = result.toString();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                s = Utils.postHttp("https://www.bing.com/ttranslatev3?", parameters);
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("xyz", "Error: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            s = decomposeJson(s);
            new Chat(s).execute();
        }
    }

    private class TranslateToEsp extends AsyncTask<Void, Void, Void>{
        HashMap<String, String> httpBodyParams;
        private String parameters, s;

        public TranslateToEsp(String message) {

            httpBodyParams = new HashMap<>();
            httpBodyParams.put("fromLang", "en");
            httpBodyParams.put("to", "es");
            httpBodyParams.put("text", message);
            Log.v("HOLA","1");
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : httpBodyParams.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");
                try {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.append("=");
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            parameters = result.toString();
            System.out.println(parameters);
            Log.v("HOLA","2");

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.v("HOLA","asdfasfasd");
                s = Utils.postHttp("https://www.bing.com/ttranslatev3?", parameters);
                System.out.println(s);
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("xyz", "Error: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            s = decomposeJson(s);
            System.out.println("TRADUCCIONESP: "+s);
            pbThink.setVisibility(View.INVISIBLE);
            talkMe(s);
        }
    }


    // https://www.bing.com/ttranslatev3?isVertical=1&&IG=C7C2278972724E04852E45BF3FA519D1&IID=translator.5026.2
    // POST
    // fromLang=es
    // text=soy programador
    // to=en



}
