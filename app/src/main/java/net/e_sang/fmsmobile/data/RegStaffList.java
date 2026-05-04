package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RegStaffList implements Parcelable {
    public String TOT_CNT = "";
    public String ROW_NO = "";
    public String FAIR_ID = "";
    public String COMPANY_STAFF_ID = "";
    public String STAFF_DEPT = "";
    public String STAFF_POSITION = "";
    public String STAFF_NAME = "";
    public String STAFF_MOBILE = "";
    public String STAFF_EMAIL = "";
    public String STAFF_PHONE = "";
    public String STAFF_ROLEs = "";
    public String STAFF_ROLEs_DESC = "";
    public String FAIR_DESC = "";
    public String FAIR_NAME = "";
    public String CREATE_DATE = "";
    public String UPDATE_DATE = "";
    public String WORK_FLAG = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.TOT_CNT);
        dest.writeString(this.ROW_NO);
        dest.writeString(this.FAIR_ID);
        dest.writeString(this.COMPANY_STAFF_ID);
        dest.writeString(this.STAFF_DEPT);
        dest.writeString(this.STAFF_POSITION);
        dest.writeString(this.STAFF_NAME);
        dest.writeString(this.STAFF_MOBILE);
        dest.writeString(this.STAFF_EMAIL);
        dest.writeString(this.STAFF_PHONE);
        dest.writeString(this.STAFF_ROLEs);
        dest.writeString(this.STAFF_ROLEs_DESC);
        dest.writeString(this.FAIR_DESC);
        dest.writeString(this.FAIR_NAME);
        dest.writeString(this.CREATE_DATE);
        dest.writeString(this.UPDATE_DATE);
        dest.writeString(this.WORK_FLAG);
    }

    public static final Creator<RegStaffList> CREATOR = new Creator<RegStaffList>() {

        public RegStaffList createFromParcel(Parcel in) {
            RegStaffList item = new RegStaffList();

            item.TOT_CNT = in.readString();
            item.ROW_NO = in.readString();
            item.FAIR_ID = in.readString();
            item.COMPANY_STAFF_ID = in.readString();
            item.STAFF_DEPT = in.readString();
            item.STAFF_POSITION = in.readString();
            item.STAFF_NAME = in.readString();
            item.STAFF_MOBILE = in.readString();
            item.STAFF_EMAIL = in.readString();
            item.STAFF_PHONE = in.readString();
            item.STAFF_ROLEs = in.readString();
            item.STAFF_ROLEs_DESC = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_NAME = in.readString();
            item.CREATE_DATE = in.readString();
            item.UPDATE_DATE = in.readString();
            item.WORK_FLAG = in.readString();
            return item;
        }

        public RegStaffList[] newArray(int size) {
            return new RegStaffList[size];
        }
    };
}