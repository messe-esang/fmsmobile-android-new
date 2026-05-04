package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceivableEventList implements Parcelable {
    public String FAIR_ID = "";
    public String FAIR_MASTER_ID = "";
    public String FAIR_DESC = "";
    public String FAIR_NAME = "";
    public String FAIR_STR_DATE = "";
    public String FAIR_END_DATE = "";
    public String CompanyCnt = "";
    public String BoothCnt = "";
    public String TotalFairAmt = "";
    public String BalanceCnt = "";
    public String BalanceAmt = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FAIR_ID);
        dest.writeString(FAIR_MASTER_ID);
        dest.writeString(FAIR_DESC);
        dest.writeString(FAIR_NAME);
        dest.writeString(FAIR_STR_DATE);
        dest.writeString(FAIR_END_DATE);
        dest.writeString(CompanyCnt);
        dest.writeString(BoothCnt);
        dest.writeString(TotalFairAmt);
        dest.writeString(BalanceCnt);
        dest.writeString(BalanceAmt);
    }

    public static final Creator<ReceivableEventList> CREATOR = new Creator<ReceivableEventList>() {

        public ReceivableEventList createFromParcel(Parcel in) {
            ReceivableEventList item = new ReceivableEventList();

            item.FAIR_ID = in.readString();
            item.FAIR_MASTER_ID = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_NAME = in.readString();
            item.FAIR_STR_DATE = in.readString();
            item.FAIR_END_DATE = in.readString();
            item.CompanyCnt = in.readString();
            item.BoothCnt = in.readString();
            item.TotalFairAmt = in.readString();
            item.BalanceCnt = in.readString();
            item.BalanceAmt = in.readString();

            return item;
        }

        public ReceivableEventList[] newArray(int size) {
            return new ReceivableEventList[size];
        }
    };
}