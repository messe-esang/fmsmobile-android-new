package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignUser implements Parcelable {
    public String SYSTEM_ID = "";
    public String USER_ID = "";
    public String NAME = "";
    public String FAIR_STATUS = "";
    public String FAIR_STATUS_DESC = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.SYSTEM_ID);
        dest.writeString(this.USER_ID);
        dest.writeString(this.NAME);
        dest.writeString(this.FAIR_STATUS);
        dest.writeString(this.FAIR_STATUS_DESC);
    }

    public static final Creator<AssignUser> CREATOR = new Creator<AssignUser>() {

        public AssignUser createFromParcel(Parcel in) {
            AssignUser item = new AssignUser();

            item.SYSTEM_ID = in.readString();
            item.USER_ID = in.readString();
            item.NAME = in.readString();
            item.FAIR_STATUS = in.readString();
            item.FAIR_STATUS_DESC = in.readString();

            return item;
        }

        public AssignUser[] newArray(int size) {
            return new AssignUser[size];
        }
    };
}
