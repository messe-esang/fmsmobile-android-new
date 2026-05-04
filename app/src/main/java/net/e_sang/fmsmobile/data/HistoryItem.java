package net.e_sang.fmsmobile.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class HistoryItem implements SearchSuggestion {

    public String ModifyDate = "";
    public String Modifyer = "";
    public String ModifyValue = "";


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

        dest.writeString(this.ModifyDate);
        dest.writeString(this.Modifyer);
        dest.writeString(this.ModifyValue);

    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<HistoryItem> CREATOR = new Creator<HistoryItem>() {

        public HistoryItem createFromParcel(Parcel in) {
            HistoryItem item = new HistoryItem();

            item.ModifyDate = in.readString();
            item.Modifyer = in.readString();
            item.ModifyValue = in.readString();

            return item;
        }

        public HistoryItem[] newArray(int size) {
            return new HistoryItem[size];
        }
    };

    @Override
    public String getBody() {
        return Modifyer;
    }
}