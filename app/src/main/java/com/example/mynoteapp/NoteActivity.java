package com.example.mynoteapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoteActivity extends AppCompatActivity {
    FloatingActionButton mcreatenotesfab;
    private FirebaseAuth firebaseAuth;
    RecyclerView mrecyclerview;
    ImageView emptyimage;
    TextView no_data;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

   FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> noteAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mcreatenotesfab = findViewById(R.id.createnotefab);
        emptyimage = findViewById(R.id.empty_imageview);
        no_data=findViewById(R.id.no_data);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarnote);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Notes");

        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(NoteActivity.this, CreateNote.class));

            }
        });



        Query query = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("time",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query, firebasemodel.class).build();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


            noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
                @Override
                protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {

                    ImageView popupbutton = noteViewHolder.itemView.findViewById(R.id.menupopbutton);

                    int colourcode = getRandomColor();

                    noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colourcode, null));

                    noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                    noteViewHolder.notecontent.setText(firebasemodel.getContent());

                    String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                    noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), NoteDetail.class);
                            intent.putExtra("title", firebasemodel.getTitle());
                            intent.putExtra("date", firebasemodel.getDate());
                            intent.putExtra("time", firebasemodel.getTime());
                            intent.putExtra("content", firebasemodel.getContent());
                            intent.putExtra("noteId", docId);

                            v.getContext().startActivity(intent);
                        }
                    });

                    popupbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                            popupMenu.setGravity(Gravity.END);

                            //Edit
                            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {

                                    Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                                    intent.putExtra("title", firebasemodel.getTitle());
                                    intent.putExtra("content", firebasemodel.getContent());
                                    intent.putExtra("noteId", docId);
                                    v.getContext().startActivity(intent);
                                    return false;
                                }
                            });
                            //share
                            popupMenu.getMenu().add("Share").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {

                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT,firebasemodel.getTitle());
                                    intent.putExtra(Intent.EXTRA_TEXT,firebasemodel.getContent());

                                    startActivity(Intent.createChooser(intent,"Share via"));
                                    return false;
                                }
                            });
                            //delete

                            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                                    builder.setTitle("Delete");
                                    builder.setMessage("Are you sure you want to delete?");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(v.getContext(), "This note is deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(v.getContext(), "Failed To Delete", Toast.LENGTH_SHORT).show();
                                                }

                                            });
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.create().show();
                                    return false;
                                }
                            });



                            popupMenu.show();
                        }
                    });


                }

                @NonNull
                @Override
                public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notelayout, parent, false);
                    return new NoteViewHolder(view);
                }

                @Override // to show image on empty view
                public void onDataChanged() {
                    if(noteAdapter.getItemCount()==0)
                    {
                            emptyimage.setVisibility(View.VISIBLE);
                            no_data.setVisibility(View.VISIBLE);
                            mrecyclerview.setVisibility(View.INVISIBLE);

                        } else {
                            emptyimage.setVisibility(View.INVISIBLE);
                            no_data.setVisibility(View.INVISIBLE);
                            mrecyclerview.setVisibility(View.VISIBLE);
                        }
                }
            };

            mrecyclerview = findViewById(R.id.recyclerview);
            mrecyclerview.setHasFixedSize(true);
            mrecyclerview.setItemAnimator(null);
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
            mrecyclerview.setAdapter(noteAdapter);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder
    {
        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle=itemView.findViewById(R.id.notetitle);
            notecontent=itemView.findViewById(R.id.notecontent);
            mnote=itemView.findViewById(R.id.note);

        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(NoteActivity.this,MainActivity.class));
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter!=null)
        {
            noteAdapter.stopListening();
        }
    }
   public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(a);
    }


    private int getRandomColor()
    {
        List<Integer> colorcode=new ArrayList<>();

        colorcode.add(R.color.lightgreen);
        colorcode.add(R.color.yell);
        colorcode.add(R.color.color3);
        colorcode.add(R.color.green);
        colorcode.add(R.color.mud);
        colorcode.add(R.color.swamp);
        colorcode.add(R.color.tint);

        Random random=new Random();
        int number=random.nextInt(colorcode.size());
        return colorcode.get(number);
    }
}