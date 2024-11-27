package com.example.mynoteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NoteDetail extends AppCompatActivity {
    private TextView mtitleofnotedetail,mcontentofnotedetail,detailnotedate,detailnotetime;
    FloatingActionButton mgotoeditnote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        mtitleofnotedetail=findViewById(R.id.titleofnotedetail);
        mcontentofnotedetail=findViewById(R.id.contentofnotedetail);
        detailnotedate=findViewById(R.id.detailnotedate);
        mgotoeditnote=findViewById(R.id.gotoeditnote);
        detailnotetime=findViewById(R.id.detailnotetime);

        Toolbar toolbar=findViewById(R.id.toolbarofnotedetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent data=getIntent();

        //redirect to edit activity
        mgotoeditnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(),EditNoteActivity.class);
                intent.putExtra("title",data.getStringExtra("title"));
                intent.putExtra("content",data.getStringExtra("content"));
                intent.putExtra("noteId",data.getStringExtra("noteId"));
                v.getContext().startActivity(intent);
            }
        });

        //displaying content and title in this activity
        mcontentofnotedetail.setText(data.getStringExtra("content"));
        detailnotedate.setText(data.getStringExtra("date"));
        detailnotetime.setText(data.getStringExtra("time"));
        mtitleofnotedetail.setText(data.getStringExtra("title"));


    }

    public void onBackPressed(){
        Intent intent = new Intent(NoteDetail.this, NoteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        else if(item.getItemId()== R.id.sharenote)
        {

            Intent data=getIntent();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT,data.getStringExtra("title"));
            intent.putExtra(Intent.EXTRA_TEXT,data.getStringExtra("content"));

            startActivity(Intent.createChooser(intent,"Share via"));
            return false;

        }

        return super.onOptionsItemSelected(item);
    }

}

