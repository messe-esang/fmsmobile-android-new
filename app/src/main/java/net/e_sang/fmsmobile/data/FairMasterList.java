package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class FairMasterList implements Parcelable {
    public String FAIR_MASTER_ID = "";
    public String FAIR_NAME = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.FAIR_MASTER_ID);
        dest.writeString(this.FAIR_NAME);
    }

    public static final Parcelable.Creator<FairMasterList> CREATOR = new Parcelable.Creator<FairMasterList>() {

        public FairMasterList createFromParcel(Parcel in) {
            FairMasterList item = new FairMasterList();

            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_NAME = in.readString();

            return item;
        }

        public FairMasterList[] newArray(int size) {
            return new FairMasterList[size];
        }
    };
}
