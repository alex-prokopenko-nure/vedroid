package com.example.notes;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private int OPEN_MESSAGE_ACTIVITY = 228;
    private int EDIT_MESSAGE_ACTIVITY = 229;
    private int MY_READ_EXTERNAL_REQUEST = 12;
    private int VERSION;
    private boolean SHOW_DOWNGRADE = false;
    private ArrayList<Note> valuesBackup = new ArrayList<Note>();
    private DBHelper dbHelper;
    private ArrayList<Migration> migrations = new ArrayList<Migration>();

    private class DateNoteComparator implements Comparator<Note> {
        @Override
        public int compare(Note o1, Note o2) {
            return o1.AppointmentDate.compareTo(o2.AppointmentDate);
        }
    }

    private class TitleComparator implements Comparator<Note> {
        @Override
        public int compare(Note o1, Note o2) {
            return o1.Title.compareTo(o2.Title);
        }
    }

    private class InsertTask extends AsyncTask<Note, Void, Void> {

        @Override
        protected Void doInBackground(Note... notes) {
            try {
                for (Note note : notes) {
                    ContentValues cv = new ContentValues();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    cv.put("title", note.Title);
                    cv.put("description", note.Description);
                    cv.put("importance", note.Importance.ordinal());
                    cv.put("imageUri", note.ImageUri);
                    cv.put("creationDate", note.CreationDate.getTime());
                    cv.put("appointmentDate", note.AppointmentDate.getTime());
                    long rowID = db.insert("notes", null, cv);
                    note.Id = (int) rowID;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class UpdateTask extends AsyncTask<Note, Void, Void> {

        @Override
        protected Void doInBackground(Note... notes) {
            try {
                for (Note note : notes) {
                    ContentValues cv = new ContentValues();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    cv.put("title", note.Title);
                    cv.put("description", note.Description);
                    cv.put("importance", note.Importance.ordinal());
                    cv.put("imageUri", note.ImageUri);
                    cv.put("creationDate", note.CreationDate.getTime());
                    cv.put("appointmentDate", note.AppointmentDate.getTime());

                    db.update("notes", cv, "id = ?", new String[] { Integer.toString(note.Id) });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class DeleteTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... ids) {
            try {
                for (int id : ids) {
                    ContentValues cv = new ContentValues();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("notes", "id = ?", new String[] { Integer.toString(id) });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "notesDB", null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table notes ("
                    + "id integer primary key autoincrement,"
                    + "title text,"
                    + "description text,"
                    + "importance integer,"
                    + "imageUri text,"
                    + "creationDate integer,"
                    + "appointmentDate integer"
                    + ");");
            db.execSQL("create table versions (id integer primary key autoincrement, version integer, downSql text)");
            for (Migration migration : migrations) {
                executeMigration(db, migration);
            }
        }

        private void executeMigration(SQLiteDatabase db, Migration migration) {
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                cv.put("version", migration.Version);
                cv.put("downSql", migration.DownSQL);
                db.insert("versions", null, cv);
                db.execSQL(migration.SQL);
                db.setVersion(migration.Version);
                db.setTransactionSuccessful();
            } catch (Exception ex){
                db.delete("versions", "version = ?", new String[] {Integer.toString(migration.Version)});
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (Migration migration : migrations) {
                if (migration.Version > oldVersion) {
                    executeMigration(db, migration);
                }
            }
        }

        private ArrayList<Migration> getMigrations(SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            ArrayList<Migration> migrations = new ArrayList<Migration>();

            Cursor c = db.query("versions", null, null, null, null, null, "version DESC");
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("id");
                int versionColIndex = c.getColumnIndex("version");
                int downSqlColIndex = c.getColumnIndex("downSql");
                do {
                    c.getInt(idColIndex);
                    Migration migration = new Migration(
                        c.getInt(versionColIndex),
                        "",
                        c.getString(downSqlColIndex)
                    );
                    migrations.add(migration);
                } while (c.moveToNext());
            }
            return migrations;
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("LogNotes", " --- database downgraded from " + oldVersion
                    + " to " + newVersion + " version --- ");
            ArrayList<Migration> dbMigrations = getMigrations(db);

            for (Migration migration : dbMigrations) {
                if (migration.Version > newVersion) {
                    db.beginTransaction();
                    try {
                        db.delete("versions", "version = ?", new String[] {Integer.toString(migration.Version)});
                        db.execSQL(migration.DownSQL);
                        db.setVersion(migration.Version);
                        db.setTransactionSuccessful();
                        SHOW_DOWNGRADE = true;
                    } catch (Exception ex) {
                        ContentValues cv = new ContentValues();
                        cv.put("version", migration.Version);
                        cv.put("downSql", migration.DownSQL);
                        db.insert("versions", null, cv);
                    }finally {
                        db.endTransaction();
                    }
                }
            }
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<Note> {

        private final Context context;
        private final ArrayList<Note> values;

        public StableArrayAdapter(Context context, ArrayList<Note> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        public void update(ArrayList<Note> values) {
            this.values.clear();
            String searchTerm = ((EditText)findViewById(R.id.editText6)).getText().toString();
            Importance importance = Importance.values()[((Spinner)findViewById(R.id.spinner6)).getSelectedItemPosition()];
            Sorting sorting = Sorting.values()[((Spinner)findViewById(R.id.spinner7)).getSelectedItemPosition()];
            searchTerm = ".*" + searchTerm + ".*";
            Pattern pattern = Pattern.compile(searchTerm);
            for (int i = 0; i < values.size(); ++i) {
                Note currentNote = values.get(i);
                boolean shouldAdd = true;
                if (importance != Importance.NONE && currentNote.Importance != importance) {
                    shouldAdd = false;
                }
                if (!searchTerm.isEmpty()) {
                    Matcher matcher = pattern.matcher(currentNote.Title);
                    if (!matcher.find()) {
                        shouldAdd = false;;
                    }
                }
                if (shouldAdd)
                    this.values.add(currentNote);
            }
            switch (sorting) {
                case DATE:
                    this.values.sort(new DateNoteComparator());
                    break;
                case ALPHA:
                    this.values.sort(new TitleComparator());
            }
        }

        public void updateAt(int i, Note note) {
            values.set(i, note);
        }

        public void delete(int i) {
            values.remove(i);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.note, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            TextView descriptionView = (TextView) view.findViewById(R.id.textView3);
            TextView dateView = (TextView) view.findViewById(R.id.textView4);
            ImageView imgView = (ImageView) view.findViewById(R.id.imageView5);
            ImageView iconView = (ImageView) view.findViewById(R.id.imageView4);

            Note note = values.get(position);

            textView.setText(note.Title);
            descriptionView.setText(note.Description);
            dateView.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(note.AppointmentDate));
            if (note.ImageUri != null) {
                if (checkSelfPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_EXTERNAL_REQUEST);
                } else {
                    try {
                        imgView.setImageBitmap(BitmapFactory.decodeFile(note.ImageUri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (note.Importance != null) {
                switch (note.Importance) {
                    case LOW:
                        iconView.setImageResource(R.drawable.trivial);
                        break;
                    case MEDIUM:
                        iconView.setImageResource(R.drawable.important);
                        break;
                    case HIGH:
                        iconView.setImageResource(R.drawable.veryimportant);
                        break;
                }
            }

            if (position == values.size()) {
                loadFromDB();
            }

            return view;
        }

    }

    private AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final ListView listview = (ListView) findViewById(R.id.listview);
            StableArrayAdapter adapter = (StableArrayAdapter)listview.getAdapter();
            adapter.update(valuesBackup);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        migrations.add(new Migration(2, "create table gg (id integer primary key autoincrement)", "drop table gg"));
        migrations.add(new Migration(3, "create table dummy (id integer primary key autoincrement)", "drop table dummy"));
        VERSION = migrations.get(migrations.size() - 1).Version;
        dbHelper = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Spinner spinner = (Spinner) findViewById(R.id.spinner6);
        spinner.setAdapter(new ArrayAdapter<Importance>(this, android.R.layout.simple_spinner_item, Importance.values()));
        spinner.setOnItemSelectedListener(spinnerListener);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner7);
        spinner2.setAdapter(new ArrayAdapter<Sorting>(this, android.R.layout.simple_spinner_item, Sorting.values()));
        spinner2.setOnItemSelectedListener(spinnerListener);
        loadFromDB();
        ArrayList<Note> list = new ArrayList<Note>();
        final ListView listview = (ListView) findViewById(R.id.listview);
        final StableArrayAdapter adapter = new StableArrayAdapter(this, list);
        adapter.update(valuesBackup);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                Intent intent = new Intent(HomeActivity.this, DisplayMessageActivity.class);
                intent.putExtra("noteModel", (Note)adapter.getItemAtPosition(position));
                intent.putExtra("position", ((Note)adapter.getItemAtPosition(position)).Id);
                startActivityForResult(intent, EDIT_MESSAGE_ACTIVITY);
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Warning")
                        .setMessage("Do you really want to delete this item?")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                final ListView listview = (ListView) findViewById(R.id.listview);
                                StableArrayAdapter adapter = (StableArrayAdapter)listview.getAdapter();
                                delete(adapter.values.get(pos).Id);
                                adapter.delete(pos);
                                adapter.notifyDataSetChanged();
                                save(valuesBackup);
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
        EditText searchBar = findViewById(R.id.editText6);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                final ListView listview = (ListView) findViewById(R.id.listview);
                StableArrayAdapter adapter = (StableArrayAdapter)listview.getAdapter();
                adapter.update(valuesBackup);
                adapter.notifyDataSetChanged();
            }
        });

        if (SHOW_DOWNGRADE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Warning")
                    .setMessage("Database on your device was newer than application's one")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (valuesBackup == null || valuesBackup.size() == 0) {
            return;
        }
        Gson gson = new Gson();
        String jsonString = gson.toJson(valuesBackup);
        try {
            File file = getFilesDir();
            OutputStream os = openFileOutput("data.json", MODE_APPEND);
            BufferedWriter lout = new BufferedWriter(new OutputStreamWriter(os));
            lout.write(jsonString);
            os.close();
        } catch (Exception e) {
            int a = 0;
        }
    }

    public void save(Object object) {
        File path = getFilesDir();
        File file = new File(path, "hata.json");
        Gson gson = new Gson();
        String jsonString = gson.toJson(object);
        try (FileOutputStream fos = new FileOutputStream(file)){
            fos.write(jsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File path = getFilesDir();
        File file = new File(path, "hata.json");
        int length = (int)file.length();
        byte[] bytes = new byte[length];
        Gson gson = new Gson();
        try (FileInputStream fis = new FileInputStream(file)){
            fis.read(bytes);
            String jsonString = new String(bytes);
            Type listType = new TypeToken<ArrayList<Note>>(){}.getType();
            valuesBackup = gson.fromJson(jsonString, listType);
            if (valuesBackup == null) {
                valuesBackup = new ArrayList<Note>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(Note note) {
        InsertTask task = new InsertTask();
        task.execute(note);
    }

    public void update(Note note) {
        UpdateTask task = new UpdateTask();
        task.execute(note);
    }

    public void delete(int id) {
        DeleteTask task = new DeleteTask();
        task.execute(id);
    }

    public void loadFromDB() {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.query("notes", null, null, null, null, null, null);
        if (valuesBackup == null) {
            valuesBackup = new ArrayList<Note>();
        }
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("id");
            int titleColIndex = c.getColumnIndex("title");
            int descriptionColIndex = c.getColumnIndex("description");
            int importanceColIndex = c.getColumnIndex("importance");
            int imageUriColIndex = c.getColumnIndex("imageUri");
            int creationDateColIndex = c.getColumnIndex("creationDate");
            int appointmentDateColIndex = c.getColumnIndex("appointmentDate");

            do {
                Note note = new Note(
                    c.getInt(idColIndex),
                    c.getString(titleColIndex),
                    c.getString(descriptionColIndex),
                    Importance.values()[c.getInt(importanceColIndex)],
                    c.getString(imageUriColIndex),
                    new Date(c.getLong(creationDateColIndex)),
                    new Date(c.getLong(appointmentDateColIndex))
                );
                valuesBackup.add(note);
            } while (c.moveToNext());
        }

        dbHelper.close();
    }

    public void onCreateNote(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra("intent", (String)null);
        intent.putExtra("position", valuesBackup.size());
        startActivityForResult(intent, OPEN_MESSAGE_ACTIVITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_MESSAGE_ACTIVITY && resultCode == RESULT_OK && data != null) {
            try {
                Note note = (Note) data.getParcelableExtra("noteModel");
                final ListView listview = (ListView) findViewById(R.id.listview);
                StableArrayAdapter adapter = (StableArrayAdapter)listview.getAdapter();
                insert(note);
                valuesBackup.add(note);
                adapter.update(valuesBackup);
                adapter.notifyDataSetChanged();
                // save(valuesBackup);
            } catch (Exception ex) {

            }
        }
        if (requestCode == EDIT_MESSAGE_ACTIVITY && resultCode == RESULT_OK && data != null) {
            try {
                Note note = (Note) data.getParcelableExtra("noteModel");
                int position = data.getIntExtra("position", -1);
                final ListView listview = (ListView) findViewById(R.id.listview);
                StableArrayAdapter adapter = (StableArrayAdapter)listview.getAdapter();
                update(note);
                int pos = -1;
                for (int i = 0; i < valuesBackup.size(); ++i) {
                    if (valuesBackup.get(i).Id == position) {
                        pos = i;
                        break;
                    }
                }
                valuesBackup.set(pos, note);
                adapter.update(valuesBackup);
                adapter.notifyDataSetChanged();
                // save(valuesBackup);
            } catch (Exception ex) {

            }
        }
    }
}
