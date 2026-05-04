package net.e_sang.fmsmobile.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.RegStaffList;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class CompanyBasicInfoFragment extends Fragment implements OnClickListener , TelKit.OnResultListener {
    public CompanyInfo mCompanyInfo = null;
    private View mRootView = null;

    private RecyclerView mRecyclerView = null;
    private ArrayList<RegStaffList> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private TextView txtCompanyName = null;
    private TextView txtBrandName = null;
    private TextView txtShopName = null;
    private TextView txtBizNum = null;
    private TextView txtRepresentation = null;
    private TextView txtAddr = null;
    private TextView txtItemName = null;
    private TextView txtHomepage = null;

    private TextView txtUSER_ID = null;
    private Button btnResetPassword = null;

    public static CompanyBasicInfoFragment getInstance(CompanyInfo companyInfo) {
        CompanyBasicInfoFragment fragment = new CompanyBasicInfoFragment(companyInfo);
        return fragment;
    }

    protected CompanyBasicInfoFragment(CompanyInfo companyInfo) {
        this.mCompanyInfo = companyInfo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_company_basic_info, null);
        }

        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mTxtEmpty = mRootView.findViewById(R.id.txtEmpty);
        txtCompanyName = mRootView.findViewById(R.id.txtCompanyName);
        txtBrandName = mRootView.findViewById(R.id.txtBrandName);
        txtShopName = mRootView.findViewById(R.id.txtShopName);
        txtBizNum = mRootView.findViewById(R.id.txtBizNum);
        txtRepresentation = mRootView.findViewById(R.id.txtRepresentation);
        txtAddr = mRootView.findViewById(R.id.txtAddr);
        txtItemName = mRootView.findViewById(R.id.txtItemName);
        txtHomepage = mRootView.findViewById(R.id.txtHomepage);
        txtUSER_ID = mRootView.findViewById(R.id.txtUSER_ID);
        btnResetPassword = mRootView.findViewById(R.id.btnResetPassword);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration did = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(did);
        mAdapter = new RecycleAdapter(getContext(), mItems);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        btnResetPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCompanyInfo.USER_ID.isEmpty()) {
                    Kit.showAlertDialog(getContext(), "", "아이디를 먼저 생성하세요.", "확인");
                } else {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getContext());
                    alert_confirm.setMessage("비밀번호를 초기화 하시겠습니까?").setCancelable(false).setPositiveButton("네",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    ResetPassword();
                                }
                            }).setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        });

        return mRootView;
    }

    protected void ResetPassword(){
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_ID", String.valueOf(mCompanyInfo.COMPANY_ID));
        body.put("USER_ID", mCompanyInfo.USER_ID);
        new TelKit(getContext(),this ).request(TelKit.URL_API_PASSWORD_RESET, body);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Activity activity = getActivity();
        View view = getView();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        // TODO Auto-generated method stub
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
    }

    // OnClickListener
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {

            default:
                break;
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_PASSWORD_RESET)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");

                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<RegStaffList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<RegStaffList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_staff, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            RegStaffList staff = mItemList.get(position);

            holder.txtName.setText(staff.STAFF_NAME);
            holder.txtPosition.setText(staff.STAFF_POSITION);
            holder.txtPhoneNum.setText(staff.STAFF_PHONE);
            Kit.addLinksPhoneNumbers(holder.txtPhoneNum);
            holder.txtMobileNum.setText(staff.STAFF_MOBILE);
            Kit.addLinksPhoneNumbers(holder.txtMobileNum);
            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            mTxtEmpty.setVisibility(count > 0 ? View.INVISIBLE : View.VISIBLE);

            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(RecycleAdapter.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RecycleAdapter mAdapter;
            public LinearLayout layoutItem;
            public TextView txtName, txtPosition, txtPhoneNum, txtMobileNum;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.txtName = itemView.findViewById(R.id.txtName);
                this.txtPosition = itemView.findViewById(R.id.txtPosition);
                this.txtPhoneNum = itemView.findViewById(R.id.txtPhoneNum);
                this.txtMobileNum = itemView.findViewById(R.id.txtMobileNum);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void load() {
        //
        txtShopName = mRootView.findViewById(R.id.txtShopName);
        txtBizNum = mRootView.findViewById(R.id.txtBizNum);
        txtRepresentation = mRootView.findViewById(R.id.txtRepresentation);
        txtAddr = mRootView.findViewById(R.id.txtAddr);
        txtItemName = mRootView.findViewById(R.id.txtItemName);
        txtHomepage = mRootView.findViewById(R.id.txtHomepage);
        txtUSER_ID = mRootView.findViewById(R.id.txtUSER_ID);

        txtCompanyName.setText(mCompanyInfo.COMPANY_NAME);
        txtBrandName.setText(mCompanyInfo.BRAND_NAME);
        txtShopName.setText("");
        txtBizNum.setText(mCompanyInfo.BIZ_NO);
        txtRepresentation.setText(mCompanyInfo.CEO_NAME);
        txtAddr.setText(String.format("%s %s", mCompanyInfo.ADDR, mCompanyInfo.ADDR_DETAIL));
        txtItemName.setText(mCompanyInfo.DISPLAY_ITEMS);
        txtHomepage.setText(mCompanyInfo.HOMEPAGE);
        txtUSER_ID.setText(mCompanyInfo.USER_ID);

        mItems.clear();
        mItems.addAll(mCompanyInfo.regStaffLists);
        mAdapter.notifyDataSetChanged();
    }
}