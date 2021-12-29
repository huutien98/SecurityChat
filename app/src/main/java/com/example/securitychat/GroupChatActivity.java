package com.example.securitychat;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.securitychat.Adapters.ChatAdapter;
import com.example.securitychat.Model.MessageModel;
import com.example.securitychat.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final ArrayList<MessageModel> messageModels = new ArrayList<>();

        final String senderId = FirebaseAuth.getInstance().getUid();
        binding.userNames.setText("Friengs group");
        final ChatAdapter adapter = new ChatAdapter(messageModels, this);
        binding.chatRecycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecycleView.setLayoutManager(layoutManager);

        database.getReference().child("Group Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
                    messageModels.add(model);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = binding.eMessage.getText().toString();
                String output = loadBadWords(message);

                final MessageModel model = new MessageModel(senderId, output);
                model.setTimestamp(new Date().getTime());


                binding.eMessage.setText("");

                database.getReference().child("Group Chat")
                        .push()
                        .setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
            }
            private String loadBadWords(final String input) {
                int largestWordLength = 0;

                Map<String, String[]> allBadWords = new HashMap<String, String[]>();
                int readCounter = 0;
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                    StrictMode.setThreadPolicy(policy);
                    URL url = new URL("https://blacklist-9525a.web.app/blacklist.html");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String currentLine = "";
                    while ((currentLine = reader.readLine()) != null) {
                        readCounter++;
                        String[] content;
                        try {
                            if (1 == readCounter) {
                                continue;
                            }
                            content = currentLine.split(",");
                            if (content.length == 0) {
                                continue;
                            }
                            final String word = content[0];

                            if (word.startsWith("-----")) {
                                continue;
                            }

                            if (word.length() > largestWordLength) {
                                largestWordLength = word.length();
                            }

                            String[] ignore_in_combination_with_words = new String[]{};
                            if (content.length > 1) {
                                ignore_in_combination_with_words = content[1].split("_");
                            }

                            // Make sure there are no capital letters in the spreadsheet
                            allBadWords.put(word.replaceAll("", "").toLowerCase(), ignore_in_combination_with_words);
                        } catch (Exception except) {
                        }
                    } // end while
                } catch (IOException except) {
                }
                String modifiedInput = input;

                modifiedInput = modifiedInput

                        .replaceAll("1", "i")
                        .replaceAll("!", "ị")
                        .replaceAll("3", "e")
                        .replaceAll("4", "a")
                        .replaceAll("5", "s")
                        .replaceAll("7", "t")
                        .replaceAll("0", "o")
                        .replaceAll("9", "g")
                        .replaceAll("_", " ")
                        .replaceAll("#", " ")
                        .replaceAll("$", " ");

                modifiedInput = modifiedInput.toLowerCase().replaceAll("", "");
                ArrayList<String> badWordsFound = new ArrayList<>();
                // iterate over each letter in the word
                for (int start = 0; start < modifiedInput.length(); start++) {
                    for (int offset = 1; offset < (modifiedInput.length() + 1 - start) && offset < largestWordLength; offset++) {
                        String wordToCheck = modifiedInput.substring(start, start + offset);
                        if (allBadWords.containsKey(wordToCheck)) {
                            String[] ignoreCheck = allBadWords.get(wordToCheck);
                            boolean ignore = false;
                            for (int stringIndex = 0; stringIndex < ignoreCheck.length; stringIndex++) {
                                if (modifiedInput.contains(ignoreCheck[stringIndex])) {
                                    ignore = true;
                                    break;
                                }
                            }

                            if (!ignore) {
                                badWordsFound.add(wordToCheck);
                            }
                        }
                    }
                }

                String inputToReturn = modifiedInput;
                for (String swearWord : badWordsFound) {
                    Pattern rx = Pattern.compile("\\b" + swearWord + "\\b", Pattern.CASE_INSENSITIVE);
                    //inputToReturn = rx.matcher(inputToReturn).replaceAll(new String(new char[swearWord.length()]).replace('\0', '*'));
                    char[] charsStars = new char[swearWord.length()];
                    Arrays.fill(charsStars, '*');
                    final String stars = new String(charsStars);
                    inputToReturn = rx.matcher(inputToReturn).
                            replaceAll(new String(new char[swearWord.length()]).replace("\0", "*"));
                }
                return inputToReturn;
            }
        });


    }
}