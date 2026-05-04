package net.e_sang.fmsmobile.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;

public class NameCardList implements SearchSuggestion {
    public int companyStaffId = -1;
    public String name = "";
    public String company = "";
    public String department = "";
    public String position = "";
    public String mobile = "";
    public String tel = "";
    public String email = "";
    public String address = "";
    public String homepage = "";
    public String fax = "";
    public String modifyDate = "";
    public int companyId = -1;
    public ArrayList<String> tags;
    public String image = "";
    public String memo = "";
    public String date = "";
    public String work_flag = "";

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(this.companyStaffId);
        dest.writeString(this.name);
        dest.writeString(this.company);
        dest.writeString(this.department);
        dest.writeString(this.position);
        dest.writeString(this.mobile);
        dest.writeString(this.tel);
        dest.writeString(this.email);
        dest.writeString(this.address);
        dest.writeString(this.homepage);
        dest.writeString(this.fax);
        dest.writeString(this.modifyDate);
        dest.writeInt(this.companyId);
        dest.writeStringList(this.tags);
        dest.writeString(this.image);
        dest.writeString(this.memo);
        dest.writeString(this.date);
        dest.writeString(this.work_flag);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<NameCardList> CREATOR = new Creator<NameCardList>() {

        public NameCardList createFromParcel(Parcel in) {
            NameCardList item = new NameCardList();

            item.companyStaffId = in.readInt();
            item.name = in.readString();
            item.company = in.readString();
            item.department = in.readString();
            item.position = in.readString();
            item.mobile = in.readString();
            item.tel = in.readString();
            item.email = in.readString();
            item.address = in.readString();
            item.homepage = in.readString();
            item.fax = in.readString();
            item.modifyDate = in.readString();
            item.companyId = in.readInt();
            item.tags = in.createStringArrayList();
            item.image = in.readString();
            item.memo = in.readString();
            item.date = in.readString();
            item.work_flag = in.readString();
            return item;
        }

        public NameCardList[] newArray(int size) {
            return new NameCardList[size];
        }
    };

    @Override
    public String getBody() {
        return company;
    }
}
