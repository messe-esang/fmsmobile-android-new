package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceivableList implements Parcelable {
    public String FAIR_ID = "";
    public String COMPANY_FAIR_REQ_ID = "";
    public String COMPANY_ID = "";
    public String COMPANY_NAME = "";
    public String FAIR_AMT = "";
    public String DEPOSIT_AMT = "";
    public String BALANCE_AMT = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FAIR_ID);
        dest.writeString(COMPANY_FAIR_REQ_ID);
        dest.writeString(COMPANY_ID);
        dest.writeString(COMPANY_NAME);
        dest.writeString(FAIR_AMT);
        dest.writeString(DEPOSIT_AMT);
        dest.writeString(BALANCE_AMT);
    }

    public static final Creator<ReceivableList> CREATOR = new Creator<ReceivableList>() {

        public ReceivableList createFromParcel(Parcel in) {
            ReceivableList item = new ReceivableList();

            item.FAIR_ID = in.readString();
            item.COMPANY_FAIR_REQ_ID = in.readString();
            item.COMPANY_ID = in.readString();
            item.COMPANY_NAME = in.readString();
            item.FAIR_AMT = in.readString();
            item.DEPOSIT_AMT = in.readString();
            item.BALANCE_AMT = in.readString();

            return item;
        }

        public ReceivableList[] newArray(int size) {
            return new ReceivableList[size];
        }
    };
}