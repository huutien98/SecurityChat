package com.example.securitychat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securitychat.Model.MessageModel;
import com.example.securitychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatAdapter extends RecyclerView.Adapter {

    private RecyclerView recyclerViewlist;
    ArrayList<MessageModel> messageModels;
    Context context;
    String recId;
    private LinearLayoutManager linearLayoutManager;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    int SENDER_VIEW_TYPE = 1;
    int RECIEVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new senderViewHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_recieve, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECIEVER_VIEW_TYPE;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModels.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new AlertDialog.Builder((context))
                        .setTitle("Delete")
                        .setMessage("Bạn có chăc chắn muốn xóa tin nhắn? ")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                database.getReference().child("chats").child(senderRoom)
                                        .child(messageModel.getMesageId())
                                        .setValue(null);

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

                return false;
            }
        });
        if (holder.getClass() == senderViewHolder.class) {
            ((senderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
        } else {
            ((RecieverViewHolder) holder).recieverMsg.setText(messageModel.getMessage());

        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
        //return 0;
    }


    public class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView recieverMsg, recievertime;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverMsg = itemView.findViewById(R.id.recieverText);
            recievertime = itemView.findViewById(R.id.recieverTime);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String time = format.format(c.getTime());
            recievertime.setText(time);

        }
    }

    public class senderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime;

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);

            senderTime = itemView.findViewById(R.id.senderTime);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String time = format.format(c.getTime());
            senderTime.setText(time);

        }
    }

}
