package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RegReceivableAmountList implements Parcelable {
    public String FAIR_NAME = "";
    public String TOT_RECV_AMT = "";
    public String PAY_DUE_DATE = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.FAIR_NAME);
        dest.writeString(this.TOT_RECV_AMT);
        dest.writeString(this.PAY_DUE_DATE);

    }

    public static final Creator<RegReceivableAmountList> CREATOR = new Creator<RegReceivableAmountList>() {

        public RegReceivableAmountList createFromParcel(Parcel in) {
            RegReceivableAmountList item = new RegReceivableAmountList();

            item.FAIR_NAME = in.readString();
            item.TOT_RECV_AMT = in.readString();
            item.PAY_DUE_DATE = in.readString();
            return item;
        }

        public RegReceivableAmountList[] newArray(int size) {
            return new RegReceivableAmountList[size];
        }
    };
}