package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ActionPlanCommentList implements Parcelable {
    public String IDX = "";
    public String COMMENT = "";
    public String DATE = "";
    public String SYSTEM_ID = "";


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(IDX);
        dest.writeString(COMMENT);
        dest.writeString(DATE);
        dest.writeString(SYSTEM_ID);


    }

    public static final Creator<ActionPlanCommentList> CREATOR = new Creator<ActionPlanCommentList>() {

        public ActionPlanCommentList createFromParcel(Parcel in) {
            ActionPlanCommentList item = new ActionPlanCommentList();

            item.IDX = in.readString();
            item.COMMENT = in.readString();
            item.DATE = in.readString();
            item.SYSTEM_ID = in.readString();

            return item;
        }

        public ActionPlanCommentList[] newArray(int size) {
            return new ActionPlanCommentList[size];
        }
    };
}
