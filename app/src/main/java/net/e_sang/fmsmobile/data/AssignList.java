package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignList implements Parcelable {
    public String FAIR_ID = "";
    public String SYSTEM_ID = "";
    public String FAIR_DESC = "";
    public String FAIR_NAME = "";
    public String USER_ID = "";
    public String NAME = "";
    public String Complete = "";
    public String AssignCnt = "";
    public String AssignRate = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.FAIR_ID);
        dest.writeString(this.SYSTEM_ID);
        dest.writeString(this.FAIR_DESC);
        dest.writeString(this.FAIR_NAME);
        dest.writeString(this.USER_ID);
        dest.writeString(this.NAME);
        dest.writeString(this.Complete);
        dest.writeString(this.AssignCnt);
        dest.writeString(this.AssignRate);
    }

    public static final Creator<AssignList> CREATOR = new Creator<AssignList>() {

        public AssignList createFromParcel(Parcel in) {
            AssignList item = new AssignList();

            item.FAIR_ID = in.readString();
            item.SYSTEM_ID = in.readString();
            item.FAIR_DESC = in.readString();
            item.FAIR_NAME = in.readString();
            item.USER_ID = in.readString();
            item.NAME = in.readString();
            item.Complete = in.readString();
            item.AssignCnt = in.readString();
            item.AssignRate = in.readString();

            return item;
        }

        public AssignList[] newArray(int size) {
            return new AssignList[size];
        }
    };
}
