package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CateList implements Parcelable {
    public String FAIR_ID = "";
    public String FAIR_MASTER_ID = "";
    public String FAIR_DESC = "";
    public String FAIR_NAME = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.FAIR_ID);
        dest.writeString(this.FAIR_MASTER_ID);
        dest.writeString(this.FAIR_DESC);
        dest.writeString(this.FAIR_NAME);
    }

    public static final Creator<CateList> CREATOR = new Creator<CateList>() {

        public CateList createFromParcel(Parcel in) {
            CateList item = new CateList();

            item.FAIR_ID = in.readString();
            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_NAME = in.readString();

            return item;
        }

        public CateList[] newArray(int size) {
            return new CateList[size];
        }
    };
}