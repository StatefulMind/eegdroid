/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.ibm.watson.developer_cloud.android.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.VibrationEffect;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;
//import android.content.Context;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.conversation.v1.model.OutputData;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import static com.ibm.watson.developer_cloud.android.myapplication.R.id.imageButton; //This is wrong.
import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import android.os.Build;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;



public class MainActivity extends AppCompatActivity {

  private final String TAG = "MainActivity";
  private EditText input;
  private ImageButton mic;
  private ImageButton conv;
  private SpeechToText speechToText;
  private static Conversation conversationService;
  private StreamPlayer player = new StreamPlayer();
  private MicrophoneHelper microphoneHelper;
  private MicrophoneInputStream capture;
  private boolean listening = false;
  private Handler handler = new Handler();
  public ListView msgView;
  public ArrayAdapter<String> msgList;
  //Map context = new HashMap();
  public Context context;
  public int counter_interactions;




  /**
   * On create.
   *
   * @param savedInstanceState the saved instance state
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    counter_interactions = 0;
    microphoneHelper = new MicrophoneHelper(this);
    speechToText = initSpeechToTextService();
    conversationService = initConversationService();
    final String inputWorkspaceId = getString(R.string.conversation_workspaceId);
    msgView = (ListView) findViewById(R.id.listView);
    msgList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    msgView.setAdapter(msgList);
    input = (EditText) findViewById(R.id.input);
    mic = (ImageButton) findViewById(R.id.mic);
    conv = (ImageButton) findViewById(imageButton);
    MessageResponse response = null;
    conversationAPI(String.valueOf(input.getText()), context, inputWorkspaceId);


    mic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // mic.setEnabled(false);
        if (!listening) {
          capture = microphoneHelper.getInputStream(true);
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {




                  speechToText.recognizeUsingWebSocket(getRecognizeOptions(capture), new MicrophoneRecognizeDelegate());
              } catch (Exception e) {
                showError(e);
              }
            }
          }).start();
          listening = true;
        } else {
          microphoneHelper.closeInputStream();
          listening = false;
        }
      }
    });



    conv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //pressing the [Send] button passes the text to the WCS conversation service
        MessageResponse response = null;
        String text = String.valueOf(input.getText());
        input.setText(String.valueOf(""));
        conversationAPI(String.valueOf(text), context, inputWorkspaceId);
      }
    });




  }

//  private void showTranslation(final String translation) {
//    runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        translatedText.setText(translation);
//      }
//    });
//  }

  private void showError(final Exception e) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
      }
    });
  }

  private void showMicText(final String text) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        input.setText(text);
      }
    });
  }

  private void enableMicButton() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mic.setEnabled(true);
      }
    });
  }

  private SpeechToText initSpeechToTextService() {
    SpeechToText service = new SpeechToText();
    String username = getString(R.string.speech_text_username);
    String password = getString(R.string.speech_text_password);
    service.setUsernameAndPassword(username, password);
    service.setEndPoint(getString(R.string.speech_text_url));
    return service;
  }

  private Conversation initConversationService() {
    Conversation service = new Conversation("2018-07-10");
    String username = getString(R.string.conversation_username);
    String password = getString(R.string.conversation_password);
    service.setUsernameAndPassword(username, password);
    service.setEndPoint(getString(R.string.conversation_url));
    return service;
  }

  private RecognizeOptions getRecognizeOptions(MicrophoneInputStream capture) {
    //return new RecognizeOptions.Builder().continuous(true).contentType(ContentType.OPUS.toString())
    //    .model("en-US_BroadbandModel").interimResults(true).inactivityTimeout(2000).build();
      return new RecognizeOptions.Builder().audio(capture).contentType(ContentType.OPUS.toString())
              .model("en-US_BroadbandModel").inactivityTimeout(2000).build();
  }

  private abstract class EmptyTextWatcher implements TextWatcher {
    private boolean isEmpty = true; // assumes text is initially empty

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (s.length() == 0) {
        isEmpty = true;
        onEmpty(true);
      } else if (isEmpty) {
        isEmpty = false;
        onEmpty(false);
      }
    }

    @Override
    public void afterTextChanged(Editable s) {}

    public abstract void onEmpty(boolean empty);
  }

  private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {

    @Override
    public void onTranscription( SpeechRecognitionResults speechResults) {
      System.out.println(speechResults);
      if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
        String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
        showMicText(text);
      }
    }

    @Override
    public void onError(Exception e) {
      showError(e);
      enableMicButton();
    }

    @Override
    public void onDisconnected() {
      enableMicButton();
    }
  }


  /**
   * On request permissions result.
   *
   * @param requestCode the request code
   * @param permissions the permissions
   * @param grantResults the grant results
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case MicrophoneHelper.REQUEST_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  /**
   * On activity result.
   *
   * @param requestCode the request code
   * @param resultCode the result code
   * @param data the data
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }


  public void conversationAPI(String input, Context context, String workspaceId) {

      InputData.Builder inputDataBuilder = new InputData.Builder(input);
      InputData inputData = inputDataBuilder.build();

    //conversationService
    //MessageRequest newMessage = new MessageRequest().setInput(inputData).getContext(context).build();
      MessageRequest newMessage = new MessageRequest();
      newMessage.setInput(inputData);
      newMessage.setContext(context);
    if (input.trim().length() > 0) {
        input = "User: " + input;
        msgList.add(input);
        msgView.setAdapter(msgList);
    };

    MessageOptions messageOptions = new MessageOptions.Builder().workspaceId(workspaceId).messageRequest(newMessage).context(context).input(inputData).build();


    conversationService.message(messageOptions).enqueue(new ServiceCallback<MessageResponse>() {
      @Override
      public void onResponse(MessageResponse response) {
        //output to system log output, just for verification/checking
        //System.out.println(response);
        displayMsg(response);
        //msgView.smoothScrollToPosition(msgList.getCount());

      }
      @Override
      public void onFailure(Exception e) {
        showError(e);
      }
    });
  };



    public void displayMsg(MessageResponse msg) {
        final MessageResponse mssg=msg;
        OutputData outputData = new OutputData();
        outputData = mssg.getOutput();

        //Acá va el if según el tipo de output
        final String text = outputData.getText().get(0);

        final String[] textSplit = text.split(Pattern.quote(". "));
        final List<String> texts = Arrays.asList(textSplit);

        for (final String element : texts) {

            int delay = 750 * (element.length()/25);
            try {
                Thread.sleep(delay);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                  msgList.add("EpiBot: " + element);
                  msgView.setAdapter(msgList);
                  context = mssg.getContext();
                }
            });
        }
    };

}