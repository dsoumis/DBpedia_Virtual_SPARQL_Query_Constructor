package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomParcelablePairDto implements Parcelable {

    private String firstValue;
    private String secondValue;

    public CustomParcelablePairDto(final String firstValue, final String secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    protected CustomParcelablePairDto(Parcel in) {
        this.firstValue = in.readString();
        this.secondValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstValue);
        dest.writeString(secondValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CustomParcelablePairDto> CREATOR = new Creator<CustomParcelablePairDto>() {
        @Override
        public CustomParcelablePairDto createFromParcel(Parcel in) {
            return new CustomParcelablePairDto(in);
        }

        @Override
        public CustomParcelablePairDto[] newArray(int size) {
            return new CustomParcelablePairDto[size];
        }
    };

    public String getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(String firstValue) {
        this.firstValue = firstValue;
    }

    public String getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(String secondValue) {
        this.secondValue = secondValue;
    }
}
