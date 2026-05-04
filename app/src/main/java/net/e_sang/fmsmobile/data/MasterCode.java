package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MasterCode implements Parcelable {
    public String VALUE = "";
    public String DESC = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.VALUE);
        dest.writeString(this.DESC);
    }

    public static final Parcelable.Creator<MasterCode> CREATOR = new Parcelable.Creator<MasterCode>() {

        public MasterCode createFromParcel(Parcel in) {
            MasterCode item = new MasterCode();

            item.VALUE = in.readString();
            item.DESC = in.readString();

            return item;
        }

        public MasterCode[] newArray(int size) {
            return new MasterCode[size];
        }
    };
}