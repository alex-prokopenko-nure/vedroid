package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.SeekBar;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ColorModel color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        color = new ColorModel();
        if (savedInstanceState != null) {
            color.setRed(savedInstanceState.getInt("red"));
            color.setGreen(savedInstanceState.getInt("green"));
            color.setBlue(savedInstanceState.getInt("blue"));
        }
        color.calculateColor();

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setColorModel(color);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("red", color.getRed());
        outState.putInt("green", color.getGreen());
        outState.putInt("blue", color.getBlue());
    }
}
