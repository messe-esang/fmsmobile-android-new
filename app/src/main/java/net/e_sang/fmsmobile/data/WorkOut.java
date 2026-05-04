package net.e_sang.fmsmobile.data;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkOut implements Parcelable {
    public String TOT_CNT = "";
    public String ROW_NO = "";
    public String FMS_USER_NAME = "";
    public String WOID = "";
    public String WORK_CONTENT = "";
    public String CREATE_DATE = "";
    public String IP_ADDRESS = "";
    public String LATITUDE = "";
    public String LONGITUDE = "";
    public String START_WORK_DATETIME = "";
    public String END_WORK_DATETIME = "";
    public String UPDATE_DATETIME = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TOT_CNT);
        dest.writeString(ROW_NO);
        dest.writeString(FMS_USER_NAME);
        dest.writeString(WOID);
        dest.writeString(WORK_CONTENT);
        dest.writeString(CREATE_DATE);
        dest.writeString(IP_ADDRESS);
        dest.writeString(LATITUDE);
        dest.writeString(LONGITUDE);
        dest.writeString(START_WORK_DATETIME);
        dest.writeString(END_WORK_DATETIME);
        dest.writeString(UPDATE_DATETIME);

    }

    public static final Creator<WorkOut> CREATOR = new Creator<WorkOut>() {

        public WorkOut createFromParcel(Parcel in) {
            WorkOut item = new WorkOut();

            item.TOT_CNT = in.readString();
            item.ROW_NO = in.readString();
            item.FMS_USER_NAME = in.readString();
            item.WOID = in.readString();
            item.WORK_CONTENT = in.readString();
            item.CREATE_DATE = in.readString();
            item.IP_ADDRESS = in.readString();
            item.LATITUDE = in.readString();
            item.LONGITUDE = in.readString();
            item.START_WORK_DATETIME = in.readString();
            item.END_WORK_DATETIME = in.readString();
            item.UPDATE_DATETIME = in.readString();

            return item;
        }

        public WorkOut[] newArray(int size) {
            return new WorkOut[size];
        }
    };
}
