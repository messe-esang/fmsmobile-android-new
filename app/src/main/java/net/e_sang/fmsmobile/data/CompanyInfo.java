package net.e_sang.fmsmobile.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;

public class CompanyInfo implements SearchSuggestion {
    public int COMPANY_ID = -1;
    public String COMPANY_NAME = "";
    public String BRAND_NAME = "";
    public String BIZ_NO = "";
    public String FAIR_STATUS_DESC = "";
    public String CATEGORY_DESC = "";
    public String DISPLAY_ITEMS = "";
    public String CEO_NAME = "";
    public String STAFF_NAME = "";
    public String STAFF_MOBILE = "";
    public int ROLE = -1;
    public String ZIP_CODE = "";
    public String ADDR = "";
    public String ADDR_DETAIL = "";
    public String FAIR_HISTORY = "";
    public String USER_ID = "";
    public String PASSWORD = "";
    public String CONTENT = "";
    public int INFLOW_PATH = -1;
    public String EMAIL = "";
    public String FAX = "";
    public String HOMEPAGE = "";
    public String TEL_NO = "";
    public int RCV_AMT = -1;
    public String RCV_COLLECT_DATE = "";
    public ArrayList<RegStaffList> regStaffLists = new ArrayList<>();
    public String LAST_FAIR_DESC = "";

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(this.COMPANY_ID);
        dest.writeString(this.COMPANY_NAME);
        dest.writeString(this.BRAND_NAME);
        dest.writeString(this.BIZ_NO);
        dest.writeString(this.FAIR_STATUS_DESC);
        dest.writeString(this.CATEGORY_DESC);
        dest.writeString(this.DISPLAY_ITEMS);
        dest.writeString(this.CEO_NAME);
        dest.writeString(this.STAFF_NAME);
        dest.writeString(this.STAFF_MOBILE);
        dest.writeInt(this.ROLE);
        dest.writeString(this.ZIP_CODE);
        dest.writeString(this.ADDR);
        dest.writeString(this.ADDR_DETAIL);
        dest.writeString(this.FAIR_HISTORY);
        dest.writeString(this.USER_ID);
        dest.writeString(this.PASSWORD);
        dest.writeString(this.CONTENT);
        dest.writeInt(this.INFLOW_PATH);
        dest.writeString(this.EMAIL);
        dest.writeString(this.FAX);
        dest.writeString(this.HOMEPAGE);
        dest.writeString(this.TEL_NO);
        dest.writeInt(this.RCV_AMT);
        dest.writeString(this.RCV_COLLECT_DATE);
        dest.writeList(this.regStaffLists);
        dest.writeString(this.LAST_FAIR_DESC);
    }

    ;

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<CompanyInfo> CREATOR = new Creator<CompanyInfo>() {

        public CompanyInfo createFromParcel(Parcel in) {
            CompanyInfo item = new CompanyInfo();

            item.COMPANY_ID = in.readInt();
            item.COMPANY_NAME = in.readString();
            item.BRAND_NAME = in.readString();
            item.BIZ_NO = in.readString();
            item.FAIR_STATUS_DESC = in.readString();
            item.CATEGORY_DESC = in.readString();
            item.DISPLAY_ITEMS = in.readString();
            item.CEO_NAME = in.readString();
            item.STAFF_NAME = in.readString();
            item.STAFF_MOBILE = in.readString();
            item.ROLE = in.readInt();
            item.ZIP_CODE = in.readString();
            item.ADDR = in.readString();
            item.ADDR_DETAIL = in.readString();
            item.FAIR_HISTORY = in.readString();
            item.USER_ID = in.readString();
            item.PASSWORD = in.readString();
            item.CONTENT = in.readString();
            item.INFLOW_PATH = in.readInt();
            item.EMAIL = in.readString();
            item.FAX = in.readString();
            item.HOMEPAGE = in.readString();
            item.TEL_NO = in.readString();
            item.RCV_AMT = in.readInt();
            item.RCV_COLLECT_DATE = in.readString();
            item.regStaffLists = in.readArrayList(null);
            item.LAST_FAIR_DESC = in.readString();

            return item;
        }

        public CompanyInfo[] newArray(int size) {
            return new CompanyInfo[size];
        }
    };

    @Override
    public String getBody() {
        return COMPANY_NAME;
    }
}
