package net.e_sang.fmsmobile.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;

public class CompanyStaffInfo implements SearchSuggestion {
    public int COMPANY_STAFF_ID = -1;
    public int COMPANY_ID = -1;
    public String COMPANY_NAME = "";
    public String STAFF_NAME = "";
    public String STAFF_MOBILE = "";
    public String STAFF_PHONE = "";
    public String STAFF_EMAIL = "";
    public String STAFF_DEPT = "";
    public String STAFF_POSITION = "";
    public String UPDATE_DATE = "";
    public String NAMECARD_URL = "";
    public int TM_COUNT = -1;
    public String WORK_FLAG = "";

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(this.COMPANY_STAFF_ID);
        dest.writeInt(this.COMPANY_ID);
        dest.writeString(this.COMPANY_NAME);
        dest.writeString(this.STAFF_NAME);
        dest.writeString(this.STAFF_MOBILE);
        dest.writeString(this.STAFF_PHONE);
        dest.writeString(this.STAFF_EMAIL);
        dest.writeString(this.STAFF_DEPT);
        dest.writeString(this.STAFF_POSITION);
        dest.writeString(this.UPDATE_DATE);
        dest.writeString(this.NAMECARD_URL);
        dest.writeInt(this.TM_COUNT);
        dest.writeString(this.WORK_FLAG);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<CompanyStaffInfo> CREATOR = new Creator<CompanyStaffInfo>() {

        public CompanyStaffInfo createFromParcel(Parcel in) {
            CompanyStaffInfo item = new CompanyStaffInfo();

            item.COMPANY_STAFF_ID = in.readInt();
            item.COMPANY_ID = in.readInt();
            item.COMPANY_NAME = in.readString();
            item.STAFF_NAME = in.readString();
            item.STAFF_MOBILE = in.readString();
            item.STAFF_PHONE = in.readString();
            item.STAFF_EMAIL = in.readString();
            item.STAFF_DEPT = in.readString();
            item.STAFF_POSITION = in.readString();
            item.UPDATE_DATE = in.readString();
            item.NAMECARD_URL = in.readString();
            item.TM_COUNT = in.readInt();
            item.WORK_FLAG = in.readString();
            return item;
        }

        public CompanyStaffInfo[] newArray(int size) {
            return new CompanyStaffInfo[size];
        }
    };

    @Override
    public String getBody() {
        return STAFF_NAME;
    }
}
