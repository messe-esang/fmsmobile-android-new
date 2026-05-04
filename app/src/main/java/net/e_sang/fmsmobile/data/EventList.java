package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class EventList implements Parcelable {
    public String TOT_CNT = "";
    public String ROW_NO = "";
    public String FAIR_ID = "";
    public String FAIR_MASTER_ID = "";
    //public String FAIR_YEAR = "";
    public String FAIR_DESC = "";
    public String FAIR_NAME = "";
    //public String TEAM = "";
    public String FAIR_STR_DATE = "";
    public String FAIR_END_DATE = "";
    //public String FAIR_STATUS_DESC = "";
    //public String CompanyCnt = "";
    //public String BoothCnt = "";
    //public String TotalFairAmt = "";
    //public String BalanceCnt = "";
    //public String BalanceAmt = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TOT_CNT);
        dest.writeString(ROW_NO);
        dest.writeString(FAIR_ID);
        dest.writeString(FAIR_MASTER_ID);
        //dest.writeString(FAIR_YEAR);
        dest.writeString(FAIR_DESC);
        dest.writeString(FAIR_NAME);
        //dest.writeString(TEAM);
        dest.writeString(FAIR_STR_DATE);
        dest.writeString(FAIR_END_DATE);
        //dest.writeString(FAIR_STATUS_DESC);
        //dest.writeString(CompanyCnt);
        //dest.writeString(BoothCnt);
        //dest.writeString(TotalFairAmt);
        //dest.writeString(BalanceCnt);
        //dest.writeString(BalanceAmt);
    }

    public static final Creator<EventList> CREATOR = new Creator<EventList>() {

        public EventList createFromParcel(Parcel in) {
            EventList item = new EventList();

            item.TOT_CNT = in.readString();
            item.ROW_NO = in.readString();
            item.FAIR_ID = in.readString();
            item.FAIR_MASTER_ID = in.readString();
            //item.FAIR_YEAR = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_NAME = in.readString();
            //item.TEAM = in.readString();
            item.FAIR_STR_DATE = in.readString();
            item.FAIR_END_DATE = in.readString();
            //item.FAIR_STATUS_DESC = in.readString();
            //item.CompanyCnt = in.readString();
            //item.BoothCnt = in.readString();
            //item.TotalFairAmt = in.readString();
            //item.BalanceCnt = in.readString();
            //item.BalanceAmt = in.readString();

            return item;
        }

        public EventList[] newArray(int size) {
            return new EventList[size];
        }
    };
}
