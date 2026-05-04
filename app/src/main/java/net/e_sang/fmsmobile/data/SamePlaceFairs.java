package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SamePlaceFairs implements Parcelable {
    public String FAIR_ID = "";
    public String FAIR_DESC = "";
    public String FAIR_SHORT_DESC = "";
    private boolean selected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.FAIR_ID);
        dest.writeString(this.FAIR_DESC);
        dest.writeString(this.FAIR_SHORT_DESC);
    }

    public static final Creator<SamePlaceFairs> CREATOR = new Creator<SamePlaceFairs>() {

        public SamePlaceFairs createFromParcel(Parcel in) {
            SamePlaceFairs item = new SamePlaceFairs();

            item.FAIR_ID = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_SHORT_DESC = in.readString();

            return item;
        }

        public SamePlaceFairs[] newArray(int size) {
            return new SamePlaceFairs[size];
        }
    };

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}