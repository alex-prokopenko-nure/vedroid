package com.example.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {
    CalculatorModel calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calculator = new CalculatorModel();
        setContentView(R.layout.activity_calculator);
        if (savedInstanceState != null) {
            CharSequence text = savedInstanceState.getCharSequence("calcText");
            EditText calcText = findViewById(R.id.editText);
            calcText.setText(text);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText calcText = findViewById(R.id.editText);
        CharSequence text = calcText.getText();
        outState.putCharSequence("calcText", text);
    }

    // adds symbol from a button to calculator line
    public void printText(View v) {
        Button button = findViewById(v.getId());
        String str = button.getText().toString();
        EditText calcText = findViewById(R.id.editText);
        CharSequence text = calcText.getText();
        if (text.toString().equals("Wrong format")) {
            calcText.setText(str);
        } else {
            CharSequence newText = text + str;
            calcText.setText(newText);
        }
    }

    // deletes symbol from a button to calculator line
    public void backspace(View v) {
        EditText calcText = findViewById(R.id.editText);
        CharSequence text = calcText.getText();
        if (text.toString().equals("Wrong format"))
            calcText.setText("");
        else if (text.length() != 0)
            calcText.setText(text.subSequence(0, text.length() - 1));
    }

    // calculates math expression and writes value to calculator line
    public void calculate(View v) {
        EditText calcText = findViewById(R.id.editText);
        try {
            int value = evaluate(calcText.getText().toString());
            calcText.setText(String.valueOf(value));
        }
        catch (Exception ex) {
            calcText.setText("Wrong format");
        }

    }

    public int evaluate(String expression)
    {
        char[] tokens = expression.toCharArray();
        Stack<Integer> values = new Stack<Integer>();
        Stack<Character> ops = new Stack<Character>();
        boolean negateOperand = false;

        for (int i = 0; i < tokens.length; i++)
        {
            if (Character.isDigit(tokens[i]))
            {
                StringBuilder operand = new StringBuilder();
                while (true) {
                    operand.append(tokens[i]);
                    if (i + 1 >= tokens.length || !Character.isDigit(tokens[i + 1]))
                        break;
                    i++;
                }
                int value = Integer.parseInt(operand.toString());
                if (negateOperand) {
                    value = -value;
                    negateOperand = false;
                }
                values.push(value);
            }
            else if (tokens[i] == '(')
                ops.push(tokens[i]);
            else if (tokens[i] == ')')
            {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
            }
            else if (isOperand(tokens[i]))
            {
                if (tokens[i] == '-' && i < tokens.length - 1 && Character.isDigit(tokens[i + 1]) && (i == 0 || isOperand(tokens[i - 1]))) {
                    negateOperand = true;
                } else {
                    while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                        values.push(applyOp(ops.pop(), values.pop(), values.pop()));

                    ops.push(tokens[i]);
                }
            }
        }

        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));

        return values.pop();
    }

    public boolean isOperand(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    public boolean hasPrecedence(char op1, char op2)
    {
        if (op2 == '(' || op2 == ')')
            return false;
        return !((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'));
    }

    public int applyOp(char op, int b, int a)
    {
        switch (op)
        {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new
                            UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}
