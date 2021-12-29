package com.example.securitychat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.securitychat.Model.Users;
import com.example.securitychat.R;
import com.example.securitychat.databinding.FragmentStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class StatusFragment extends Fragment {
    public StatusFragment() {
        // Required empty public constructor
    }

    FirebaseDatabase database;
    FragmentStatusBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        binding.eStatus.getText().toString();
        binding.eUserName.getText().toString();

        HashMap<String, Object> obj = new HashMap<>();
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .updateChildren(obj);

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        Picasso.get().load(users.getProfilepic())
                                .placeholder(R.drawable.avatar)
                                .into(binding.profileImage);
                        binding.eStatus.setText(users.getStatus());
                        binding.eUserName.setText(users.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return binding.getRoot();
    }
}