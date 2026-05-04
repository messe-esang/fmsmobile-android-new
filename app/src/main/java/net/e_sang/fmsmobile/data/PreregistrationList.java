package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PreregistrationList implements Parcelable {
    public String FAIR_ID = "";
    public String FAIR_NAME = "";
    public String FAIR_STR_DATE = "";
    public String FAIR_END_DATE = "";
    public String PRE_VISITOR_TOT_CNT = "";
    public String NEW_PRE_VISITOR_TOT_CNT = "";
    public String FAIR_MASTER_ID = "";
    public String FAIR_PLACE = "";
    public String FAIR_SEQ = "";
    public String FAIR_PLACE_NAME = "";
    public String NEW_RATE = "";

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
        dest.writeString(PRE_VISITOR_TOT_CNT);
        dest.writeString(NEW_PRE_VISITOR_TOT_CNT);
        dest.writeString(FAIR_MASTER_ID);
        dest.writeString(FAIR_PLACE);
        dest.writeString(FAIR_SEQ);
        dest.writeString(FAIR_PLACE_NAME);
        dest.writeString(NEW_RATE);
    }

    public static final Creator<PreregistrationList> CREATOR = new Creator<PreregistrationList>() {

        public PreregistrationList createFromParcel(Parcel in) {
            PreregistrationList item = new PreregistrationList();

            item.FAIR_ID = in.readString();
            item.FAIR_NAME = in.readString();
            item.FAIR_STR_DATE = in.readString();
            item.FAIR_END_DATE = in.readString();
            item.PRE_VISITOR_TOT_CNT = in.readString();
            item.NEW_PRE_VISITOR_TOT_CNT = in.readString();
            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_PLACE = in.readString();
            item.FAIR_SEQ = in.readString();
            item.FAIR_PLACE_NAME = in.readString();
            item.NEW_RATE = in.readString();

            return item;
        }

        public PreregistrationList[] newArray(int size) {
            return new PreregistrationList[size];
        }
    };
}
