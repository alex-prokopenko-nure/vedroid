package com.example.myapplication;

import android.graphics.Color;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ColorModel extends BaseObservable {
    private int red;
    private int green;
    private int blue;
    private int color;

    @Bindable
    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
        notifyPropertyChanged(androidx.databinding.library.baseAdapters.BR.red);
        calculateColor();
    }

    @Bindable
    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
        notifyPropertyChanged(androidx.databinding.library.baseAdapters.BR.green);
        calculateColor();
    }

    @Bindable
    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
        notifyPropertyChanged(androidx.databinding.library.baseAdapters.BR.blue);
        calculateColor();
    }

    @Bindable
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        notifyPropertyChanged(androidx.databinding.library.baseAdapters.BR.color);
    }

    public void calculateColor() {
        setColor(Color.parseColor(String.format("#%02X%02X%02X", red, green, blue)));
    }
}
