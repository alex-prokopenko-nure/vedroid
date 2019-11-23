package com.example.notes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class DisplayMessageActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    private int position;
    private long time;
    private Date pickedDate;
    private String imageUri;
    private String title;
    private String description;
    private Importance noteImportance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<Importance>(this, android.R.layout.simple_spinner_item, Importance.values()));
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        if (intent.hasExtra("noteModel")) {
            Note note = intent.getParcelableExtra("noteModel");
            title = note.Title;
            description = note.Description;
            imageUri = note.ImageUri;
            pickedDate = note.AppointmentDate;
            noteImportance = note.Importance;
            ((EditText)findViewById(R.id.editText)).setText(title);
            ((EditText)findViewById(R.id.editText2)).setText(description);
            ((EditText)findViewById(R.id.editText3)).setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(pickedDate));
            spinner.setSelection(noteImportance.ordinal());
            try {
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setBackgroundColor(0xFFFFFF);
                imageView.setImageBitmap(BitmapFactory.decodeFile(imageUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPickImage(View view) {
        try {
            if (ActivityCompat.checkSelfPermission(DisplayMessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DisplayMessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            }
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean allFieldsFilled() {
        CharSequence title = ((EditText)findViewById(R.id.editText)).getText();
        CharSequence description = ((EditText)findViewById(R.id.editText2)).getText();
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        int pos = spinner.getSelectedItemPosition();
        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        this.title = title.toString();
        this.description = description.toString();
        this.noteImportance = Importance.values()[pos];

        return !title.toString().isEmpty() &&
                !description.toString().isEmpty() &&
                pickedDate != null &&
                pos != 0 &&
                imageView.getDrawable() != null;
    }

    public void setDate(Date date) {
        EditText editText = (EditText)findViewById(R.id.editText3);
        String strDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date);
        pickedDate = date;
        editText.setText(strDate);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayMessageActivity.this);
        builder.setTitle("Warning")
                .setMessage("Fill all fields please")
                .setCancelable(false)
                .setNegativeButton("Got it!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void saveNote(View view) {
        if (!allFieldsFilled()) {
            showDialog();
        } else {
            Intent intent = new Intent();
            intent.putExtra("noteModel", new Note(position, title, description, noteImportance, imageUri, pickedDate));
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            super.finish();
            this.finish();
        }
    }

    public void cancelNote(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        super.finish();
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

      if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
          Uri selectedImage = data.getData();
          String[] filePathColumn = { MediaStore.Images.Media.DATA };

          Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
          cursor.moveToFirst();

          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          imageUri = cursor.getString(columnIndex);
          Log.v("Image", "Loaded image: " + imageUri);
          cursor.close();

          ImageView imageView = findViewById(R.id.imageView);
          imageView.setBackgroundColor(0xFFFFFF);
          Bitmap bm = BitmapFactory.decodeFile(imageUri);
          imageView.setImageBitmap(BitmapFactory.decodeFile(imageUri));
        }
    }
}
