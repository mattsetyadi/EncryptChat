package com.setyadi.encryptchat;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        //intent untuk membuka grup yg ada pada grup fragment
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        //untuk koneksi database agar grup memiliki UID(unik User id)
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        //membuat referensi database
        //child "Users" adalah cabang user pada database
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //"Groups" pada child adalah bagian grup chat pada database firebase
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);




        InitializeFields();

        GetUserInfo();

        //agar send button pada chat grup bekerja dan mem validasi terlebih dahulu pada input text ada yg perlu dikirimkan atau tidak
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                //bagian atoscroll ketika ada pesan baru (fokus ke pesan terbaru paling bawah)
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }


    //perintah untuk menampilkan seluruh pesan pada grup dari database termasuk previous message yang sudah lama atau load dari database Group tertentu sesuai grup
    //dimulai dari onStart disini
    //setiap kita memulai activity kedalam group dimulai dari perintah berikut
    @Override
    protected void onStart() {
        super.onStart();

        //setiap ada perubahan dalam grup dengan perintah ini akan du update atau tampil di aplikasi(chat grup room)
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void InitializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName); //currentGroupName disini agar grup yg dibuka sesuai nama grup pada database bukan hanya tulisan "group name"

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);

    }

    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    //cek user id ada atau tidak
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SaveMessageInfoToDatabase()
    {
        //mengambil nilai dari text box input menjadi sebuah string yg akan dikirimkan ke database
        String message = userMessageInput.getText().toString();

        //protokol untuk membuat key pada database group (unik code) untuk setiap pesan dan mengelompokkannya beserta detil dibawah
        String messagekEY = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            //HashMap untuk memetakan nilai dengan key unik
            //HashMap disini digunakan juga untuk memasukan info tanggal dan waktu di atas kedalam kode unik pada database ketika di expand
            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            //ini dibawah bagian detil yang ada pada Groups>"group name">unik code>detil pesan (nama, pesan, tanggal, jam)
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);        }
    }

    //bagian memasukan perintah pada DisplayMessages untuk menampilkan pesan dalam room group chat
    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        //iterator adalah pengganti looping untuk mengakses dan menampilkan nilai
        Iterator iterator = dataSnapshot.getChildren().iterator();
        //dengan kode di atas kita akan mengambil pesan line by line dari database(from each specific group)

        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            //metode penampilan pesan
            //pengirim, next line is display message, tanggal dan waktu
            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");

            //agar ketika pesan dikirim kita dipaksa ke bootom chatjd tidak terhalang keyboard (auto scroll)
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
