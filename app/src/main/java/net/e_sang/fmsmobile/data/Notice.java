package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Notice implements Parcelable {
    public String TOT_CNT = "";
    public String ROW_NO = "";
    public String PUSH_LIST_ID = "";
    public String PUSH_TYPE = "";
    public String CODE_NAME = "";
    public String CREATE_USER = "";
    public String CREATE_DATE = "";
    public String SEND_CONTENT = "";
    public String SEND_SUBJECT = "";
    public String USER_ID = "";
    public String COMPANY_ID = "";
    public String READ_FLAG = "";
    public String FAIR_ID = "";
    public String SUBJECT = "";
    public String CONTENT = "";

    public String USER_NAME = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.TOT_CNT);
        dest.writeString(this.ROW_NO);
        dest.writeString(this.PUSH_LIST_ID);
        dest.writeString(this.PUSH_TYPE);
        dest.writeString(this.CODE_NAME);
        dest.writeString(this.CREATE_USER);
        dest.writeString(this.CREATE_DATE);
        dest.writeString(this.SEND_CONTENT);
        dest.writeString(this.SEND_SUBJECT);
        dest.writeString(this.USER_ID);
        dest.writeString(this.COMPANY_ID);
        dest.writeString(this.READ_FLAG);
        dest.writeString(this.FAIR_ID);
        dest.writeString(this.SUBJECT);
        dest.writeString(this.CONTENT);
        dest.writeString(this.USER_NAME);
    }

    public static final Creator<Notice> CREATOR = new Creator<Notice>() {

        public Notice createFromParcel(Parcel in) {
            Notice item = new Notice();

            item.TOT_CNT = in.readString();
            item.ROW_NO = in.readString();
            item.PUSH_LIST_ID = in.readString();
            item.PUSH_TYPE = in.readString();
            item.CODE_NAME = in.readString();
            item.CREATE_USER = in.readString();
            item.CREATE_DATE = in.readString();
            item.SEND_CONTENT = in.readString();
            item.SEND_SUBJECT = in.readString();
            item.USER_ID = in.readString();
            item.COMPANY_ID = in.readString();
            item.READ_FLAG = in.readString();
            item.FAIR_ID = in.readString();
            item.SUBJECT = in.readString();
            item.CONTENT = in.readString();
            item.USER_NAME = in.readString();
            return item;
        }

        public Notice[] newArray(int size) {
            return new Notice[size];
        }
    };
}