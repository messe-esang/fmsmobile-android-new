package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class FairTeam implements Parcelable {
    public String ROW_NO = "";
    public String FAIR_MASTER_ID = "";
    public String FAIR_NAME = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ROW_NO);
        dest.writeString(this.FAIR_MASTER_ID);
        dest.writeString(this.FAIR_NAME);
    }

    public static final Creator<FairTeam> CREATOR = new Creator<FairTeam>() {

        public FairTeam createFromParcel(Parcel in) {
            FairTeam item = new FairTeam();

            item.ROW_NO = in.readString();
            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_NAME = in.readString();

            return item;
        }

        public FairTeam[] newArray(int size) {
            return new FairTeam[size];
        }
    };
}