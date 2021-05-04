package com.inventerit.skychannel;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inventerit.skychannel.model.Campaign;

import java.util.List;

public class TestingClass {

    private void getUserCampaign(List<String> videoList){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");

                reference.child("id")
                .child("campaigns").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snapshot1:snapshot.getChildren()){
                            if(videoList.contains(snapshot1.child("id").getValue().toString())){
                                snapshot1.getValue(Campaign.class);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
