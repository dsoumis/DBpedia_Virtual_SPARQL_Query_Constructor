package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class DbpediaLookupResultDto implements Parcelable {
    private String label;
    private String uri;

    public DbpediaLookupResultDto(final String label, final String uri) {
        this.label = label;
        this.uri = uri;
    }

    protected DbpediaLookupResultDto(Parcel in) {
        label = in.readString();
        uri = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(uri);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DbpediaLookupResultDto> CREATOR = new Creator<DbpediaLookupResultDto>() {
        @Override
        public DbpediaLookupResultDto createFromParcel(Parcel in) {
            return new DbpediaLookupResultDto(in);
        }

        @Override
        public DbpediaLookupResultDto[] newArray(int size) {
            return new DbpediaLookupResultDto[size];
        }
    };

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbpediaLookupResultDto that = (DbpediaLookupResultDto) o;
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, uri);
    }
}
