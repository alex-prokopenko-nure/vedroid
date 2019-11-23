package com.example.notes;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Note implements Parcelable {
    public int Id;
    public String Title;
    public String Description;
    public Importance Importance;
    public String ImageUri;
    public Date CreationDate;
    public Date AppointmentDate;

    public Note(String title) {
        Title = title;
    }

    public Note(int id, String title, String description, Importance importance, String imageUri, Date appointmentDate)
    {
        Id = id;
        Title = title;
        Description = description;
        Importance = importance;
        ImageUri = imageUri;
        AppointmentDate = appointmentDate;
        CreationDate =  new Date(System.currentTimeMillis());
    }

    public Note(int id, String title, String description, Importance importance, String imageUri, Date creationDate, Date appointmentDate)
    {
        Id = id;
        Title = title;
        Description = description;
        Importance = importance;
        ImageUri = imageUri;
        AppointmentDate = appointmentDate;
        CreationDate =  creationDate;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            int id = source.readInt();
            String title = source.readString();
            String description = source.readString();
            Importance importance = com.example.notes.Importance.values()[source.readInt()];
            String uri = source.readString();
            Date creationDate = new Date(source.readLong());
            Date appointmentDate = new Date(source.readLong());
            return new Note(id, title, description, importance, uri, creationDate, appointmentDate);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Title);
        dest.writeString(Description);
        dest.writeInt(Importance.ordinal());
        dest.writeString(ImageUri);
        dest.writeLong(CreationDate.getTime());
        dest.writeLong(AppointmentDate.getTime());
    }
}
