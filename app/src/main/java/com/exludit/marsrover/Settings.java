package com.exludit.marsrover;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;

public class Settings extends AppCompatActivity {

    RadioButton rbLanguageNl;
    RadioButton rbLanguageEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rbLanguageNl = findViewById(R.id.language_nl_rb);
        rbLanguageEN = findViewById(R.id.language_en_rb);

        rbLanguageEN.setSelected(true);
    }
}
