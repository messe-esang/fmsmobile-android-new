package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class VisitorStatusList implements Parcelable {
    public String FAIR_ID = "";
    public String FAIR_NAME = "";
    public String FAIR_STR_DATE = "";
    public String FAIR_END_DATE = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FAIR_ID);
        dest.writeString(FAIR_NAME);
        dest.writeString(FAIR_STR_DATE);
        dest.writeString(FAIR_END_DATE);
    }

    public static final Creator<VisitorStatusList> CREATOR = new Creator<VisitorStatusList>() {

        public VisitorStatusList createFromParcel(Parcel in) {
            VisitorStatusList item = new VisitorStatusList();

            item.FAIR_ID = in.readString();
            item.FAIR_NAME = in.readString();
            item.FAIR_STR_DATE = in.readString();
            item.FAIR_END_DATE = in.readString();

            return item;
        }

        public VisitorStatusList[] newArray(int size) {
            return new VisitorStatusList[size];
        }
    };
}
