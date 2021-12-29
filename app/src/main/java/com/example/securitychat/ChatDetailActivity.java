package com.example.securitychat;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitychat.Adapters.ChatAdapter;
import com.example.securitychat.Model.MessageModel;
import com.example.securitychat.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

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


public class ChatDetailActivity extends AppCompatActivity {
    ArrayList<MessageModel> messageModels = new ArrayList<>();
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    private RecyclerView recyclerViewlist;
    private ScrollView scrollView;
    private String senderId, recievieId;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        final String senderId = auth.getUid();
        String recievieId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userNames.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        ArrayList<MessageModel> messageModels = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, recievieId);
        binding.chatRecycleView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecycleView.setLayoutManager(layoutManager);
        final String senderRoom = senderId + recievieId;
        final String receiverRoom = recievieId + senderId;
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);

                            model.setMesageId(snapshot1.getKey());
                            messageModels.add(model);

                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.eMessage.getText().toString();
                String output = loadBadWords(message);

                if (!message.equals("")) {
                    final MessageModel model = new MessageModel(senderId, output);
                    model.setTimestamp(new Date().getTime());
                    binding.eMessage.setText("");

                    database.getReference().
                            child("chats")
                            .child(senderRoom)
                            .push().
                            setValue(model)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .push()
                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    });
                                }
                            });

                } else {
                    Toast.makeText(ChatDetailActivity.this, "vui lòng soạn tin nhắn", Toast.LENGTH_LONG).show();
                }

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

                        .replaceAll("!", "ị")
                        .replaceAll("0", "o")
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
                    // The "(?i)" is to make the replacement case insensitive.
                    //  inputToReturn = inputToReturn.replaceAll("" + swearWord, stars);

                    inputToReturn = rx.matcher(inputToReturn).
                            replaceAll(new String(new char[swearWord.length()]).replace("\0", "*"));
                }
                return inputToReturn;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final String senderId = auth.getUid();
        String recievieId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");
        database.getReference().child("chats").child(senderId).child(recievieId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        MessageModel model = snapshot.getValue(MessageModel.class);

                        model.setMesageId(snapshot.getKey());
                        messageModels.add(model);
                        chatAdapter.notifyDataSetChanged();
                        recyclerViewlist.smoothScrollToPosition(recyclerViewlist.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}