package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AnnualVisitorStatisticsList implements Parcelable {
    public String FAIR_MASTER_ID = "";
    public String FAIR_NAME = "";
    public String FAIR_PLACE_ID = "";
    public String FAIR_PLACE = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FAIR_MASTER_ID);
        dest.writeString(FAIR_NAME);
        dest.writeString(FAIR_PLACE_ID);
        dest.writeString(FAIR_PLACE);
    }

    public static final Creator<AnnualVisitorStatisticsList> CREATOR = new Creator<AnnualVisitorStatisticsList>() {

        public AnnualVisitorStatisticsList createFromParcel(Parcel in) {
            AnnualVisitorStatisticsList item = new AnnualVisitorStatisticsList();

            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_NAME = in.readString();
            item.FAIR_PLACE_ID = in.readString();
            item.FAIR_PLACE = in.readString();

            return item;
        }

        public AnnualVisitorStatisticsList[] newArray(int size) {
            return new AnnualVisitorStatisticsList[size];
        }
    };
}
