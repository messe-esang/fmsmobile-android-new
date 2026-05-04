package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ActionPlanTeam implements Parcelable {
    public String DEPT = "";
    public String DEPT_DESC = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.DEPT);
        dest.writeString(this.DEPT_DESC);
    }

    public static final Creator<ActionPlanTeam> CREATOR = new Creator<ActionPlanTeam>() {

        public ActionPlanTeam createFromParcel(Parcel in) {
            ActionPlanTeam item = new ActionPlanTeam();

            item.DEPT = in.readString();
            item.DEPT_DESC = in.readString();

            return item;
        }

        public ActionPlanTeam[] newArray(int size) {
            return new ActionPlanTeam[size];
        }
    };
}