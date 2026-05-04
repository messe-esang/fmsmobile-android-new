package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignUserList implements Parcelable {
    public String TOT_CNT = "";
    public String ROW_NO = "";
    public String COMPANY_FAIR_REQ_ID = "";
    public String COMPANY_NAME = "";
    public String FAIR_STATUS_DESC = "";
    public String NAME = "";
    public String ASSIGN_STATUS_DESC = "";
    public String ASN_REQ_DATE = "";
    public String SYSTEM_ID = "";
    public boolean isSelected = false;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.TOT_CNT);
        dest.writeString(this.ROW_NO);
        dest.writeString(this.COMPANY_FAIR_REQ_ID);
        dest.writeString(this.COMPANY_NAME);
        dest.writeString(this.FAIR_STATUS_DESC);
        dest.writeString(this.NAME);
        dest.writeString(this.ASSIGN_STATUS_DESC);
        dest.writeString(this.ASN_REQ_DATE);
        dest.writeString(this.SYSTEM_ID);
        dest.writeInt(isSelected ? 1 : 0);
    }

    public static final Creator<AssignUserList> CREATOR = new Creator<AssignUserList>() {

        public AssignUserList createFromParcel(Parcel in) {
            AssignUserList item = new AssignUserList();

            item.TOT_CNT = in.readString();
            item.ROW_NO = in.readString();
            item.COMPANY_FAIR_REQ_ID = in.readString();
            item.COMPANY_NAME = in.readString();
            item.FAIR_STATUS_DESC = in.readString();
            item.NAME = in.readString();
            item.ASSIGN_STATUS_DESC = in.readString();
            item.ASN_REQ_DATE = in.readString();
            item.SYSTEM_ID = in.readString();
            item.isSelected = (in.readInt() == 0) ? false : true;

            return item;
        }

        public AssignUserList[] newArray(int size) {
            return new AssignUserList[size];
        }
    };
}
