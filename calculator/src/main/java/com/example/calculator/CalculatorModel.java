package com.example.calculator;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class CalculatorModel extends BaseObservable {
    private String expression;

    @Bindable
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
