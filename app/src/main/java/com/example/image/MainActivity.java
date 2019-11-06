package com.example.image;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BigView bigView = findViewById(R.id.big_view);
        try {
            bigView.setImage(getAssets().open("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
