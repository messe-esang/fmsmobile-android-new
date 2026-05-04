package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.RegStaffList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.RegSalesActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactBottomSheet extends BottomSheetDialogFragment implements TelKit.OnResultListener {

    private static final String ARG_RegStaffList = "regStaffList";
    private static final String ARG_NameCardList = "nameCardList ";

    private ArrayList<RegStaffList> mRegStaffListItems = new ArrayList<>();

    private RecyclerView recyclerView;
    private LinearLayout btn_btnAddContact_layout;
    private NameCardList mNameCardList = null;
    private int mCompanyID = 0;
    private TextView txt_name;
    private static LinearLayout empty_layout;
    private static final String TAG = "ContactBottomSheet";


    public interface OnContactSelectedListener {
        void onContactSelected(RegStaffList regStaffList);
    }

    private OnContactSelectedListener listener;

    public static ContactBottomSheet newInstance(ArrayList<RegStaffList> regStaffList, NameCardList nameCardList, int companyId, OnContactSelectedListener listener) {
        ContactBottomSheet sheet = new ContactBottomSheet();
        sheet.listener = listener;

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_RegStaffList, regStaffList);
        args.putParcelable(ARG_NameCardList, nameCardList);
        args.putInt(Extra.KEY_COMPANY_ID, companyId);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mRegStaffListItems = getArguments().getParcelableArrayList(ARG_RegStaffList);
            mNameCardList = getArguments().getParcelable(ARG_NameCardList);
            mCompanyID = getArguments().getInt(Extra.KEY_COMPANY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_contact, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        btn_btnAddContact_layout = view.findViewById(R.id.btn_btnAddContact_layout);
        txt_name = view.findViewById(R.id.txt_name);
        empty_layout = view.findViewById(R.id.empty_layout);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(new ContactAdapter(mRegStaffListItems, regStaffList -> {
            // 연락처 클릭 이벤트
            if (listener != null) {
                listener.onContactSelected(regStaffList);
            }
            dismiss();
        }));

        btn_btnAddContact_layout.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), NewCompanyActivity.class);
//            intent.putExtra("NameCard", mNameCardList);
//            intent.putExtra("entry_path", "search");
//            intent.putExtra(Extra.KEY_COMPANY_ID, mCompanyID);
//            startActivity(intent);
            btn_btnAddContact_layout.setEnabled(false);
            uploadImage(mCompanyID, mNameCardList, "");
        });

        if (null != mNameCardList) {
            Log.e(TAG, "mNameCardList.name : " + mNameCardList.name);
            Log.e(TAG, "mNameCardList.companyStaffId : " + mNameCardList.companyStaffId);
            if (mNameCardList.work_flag.equals("N")) {
                txt_name.setText("(퇴직) " + mNameCardList.name);
            } else {
                txt_name.setText(mNameCardList.name);
            }
        }
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog =
                (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet =
                    dialog.findViewById(
                            com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet =
                dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior =
                    BottomSheetBehavior.from(bottomSheet);

            // 🔒 BottomSheet 고정
            behavior.setDraggable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            // 높이 고정 (예: 화면 70%)
            int screenHeight =
                    Resources.getSystem().getDisplayMetrics().heightPixels;
            ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
            params.height = (int) (screenHeight * 0.7f);
            bottomSheet.setLayoutParams(params);
        }
//        Dialog dialog = getDialog();
//        if (dialog instanceof BottomSheetDialog) {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
//
//            FrameLayout bottomSheet =
//                    bottomSheetDialog.findViewById(
//                            com.google.android.material.R.id.design_bottom_sheet);
//
//            if (bottomSheet != null) {
//
//                BottomSheetBehavior<View> behavior =
//                        BottomSheetBehavior.from(bottomSheet);
//
//                // ⬇️ 높이 조절 (화면의 70%)
//                int screenHeight =
//                        Resources.getSystem().getDisplayMetrics().heightPixels;
//                behavior.setPeekHeight((int) (screenHeight * 0.7f));
//
//                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//                // 드래그 가능
//                behavior.setDraggable(true);
//
//                // 전체 확장 방지
//                behavior.setSkipCollapsed(false);
//                behavior.setFitToContents(true);
//            }
//        }
    }

    public static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        public interface OnItemClickListener {
            void onClick(RegStaffList regStaffList);
        }

        private List<RegStaffList> regStaffList;
        private OnItemClickListener listener;
        private int selectedPosition = -1;

        public ContactAdapter(ArrayList<RegStaffList> regStaffList, OnItemClickListener listener) {
            this.regStaffList = regStaffList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position) {

            RegStaffList mRegStaffList = regStaffList.get(position);

            if (mRegStaffList.WORK_FLAG.equals("N")) {
                holder.tvName.setText("(퇴직) " + mRegStaffList.STAFF_NAME);
            } else {
                holder.tvName.setText(mRegStaffList.STAFF_NAME);
            }

            holder.tvPhone.setText(mRegStaffList.STAFF_MOBILE);
            holder.tvEmail.setText(mRegStaffList.STAFF_EMAIL);
            if (mRegStaffList.STAFF_POSITION.isEmpty()) {
                holder.tvCompany.setText(
                        mRegStaffList.STAFF_DEPT
                );
            } else {
                holder.tvCompany.setText(
                        mRegStaffList.STAFF_DEPT + " / " + mRegStaffList.STAFF_POSITION
                );
            }

            if (Kit.isNotNullNotEmpty(mRegStaffList.UPDATE_DATE)) {
                holder.tvDate.setText("최종 수정 : " + mRegStaffList.UPDATE_DATE);
            } else {
                holder.tvDate.setText("최종 수정 : " + mRegStaffList.CREATE_DATE);
            }
            if (mRegStaffList.CREATE_DATE.isEmpty() && mRegStaffList.UPDATE_DATE.isEmpty()) {
                holder.tvDate.setVisibility(GONE);
            }

            holder.itemView.setSelected(position == selectedPosition);

            holder.itemView.setOnClickListener(v -> {
                selectedPosition = position;
                notifyDataSetChanged();
                listener.onClick(mRegStaffList);
            });
        }

        @Override
        public int getItemCount() {
            int count = regStaffList.size();
            if (count == 0) {
                empty_layout.setVisibility(View.VISIBLE);
            } else {
                empty_layout.setVisibility(View.GONE);
            }
            return count;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvPhone, tvEmail, tvCompany, tvDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvCompany = itemView.findViewById(R.id.tvCompany);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }

    private void uploadImage(int company_id, NameCardList nameCardList, String staff_id) {
        new Thread(() -> {
            try {

                File file = new File(nameCardList.image.replace("file:", ""));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // mProgressBarLayout.setVisibility(View.VISIBLE);
                    }
                });
                UserInfo userInfo = PrefKit.getUserInfo(getContext());

                HashMap<String, String> body = new HashMap<>();
                body.put("COMPANY_ID", String.valueOf(company_id));
                body.put("COMPANY_NAME", nameCardList.company);
                body.put("HOMEPAGE", nameCardList.homepage);
                body.put("ADDR", nameCardList.address);
                body.put("COMPANY_STAFF_ID", staff_id);
                body.put("STAFF_NAME", nameCardList.name);
                body.put("STAFF_EMAIL", nameCardList.email);
                body.put("STAFF_MOBILE", nameCardList.mobile);
                body.put("STAFF_PHONE", nameCardList.tel);
                body.put("STAFF_DEPT", nameCardList.department);
                body.put("STAFF_POSITION", nameCardList.position);
                body.put("USER_ID", userInfo.LOGIN_ID);
                body.put("COMPANY_STAFF_MEMO", nameCardList.memo);

                new TelKit(getContext(), result -> {
                    if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_OCR_STAFF)) {
                        try {
                            JSONObject json = new JSONObject(result.mResponse);
                            Log.e(TAG, "URL_API_INSERT_OCR_STAFF json : " + json);
                            JSONObject resultObj = json.optJSONObject("result");
                            Log.e(TAG, "URL_API_INSERT_OCR_STAFF result : " + resultObj);
                            if (resultObj != null) {
                                String code = resultObj.optString("code");
                                String msg = resultObj.optString("msg");
                                if ("success".equals(code)) {
                                    JSONObject data = json.optJSONObject("data");
                                    Log.e(TAG, "getActionPlan onResult data : " + data);
                                    assert data != null;
                                    getStaffDetail(data.optString("COMPANY_STAFF_ID"));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    // mProgressBarLayout.setVisibility(GONE);
                    btn_btnAddContact_layout.setEnabled(true);
                }).requestMultipart(
                        TelKit.URL_API_INSERT_OCR_STAFF,
                        body,
                        "namecard_image",
                        file,
                        "image/jpeg",
                        0
                );

            } catch (Exception e) {
                e.printStackTrace();
                btn_btnAddContact_layout.setEnabled(true);
            }
        }).start();
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mRequestUrl.equals(TelKit.URL_API_OCR_STAFF_DETAIL)) {
            try {
                JSONObject json = new JSONObject(result.mResponse);
                Log.e(TAG, "URL_API_OCR_STAFF_DETAIL json : " + json);
                JSONObject resultObj = json.optJSONObject("result");
                if (resultObj != null) {
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("success".equals(code)) {
                        JSONObject data = json.optJSONObject("data");
                        Log.e(TAG, "URL_API_OCR_STAFF_DETAIL data : " + data);
                        assert data != null;
                        showManagerCompleteDialog(data);
                    } else {
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getStaffDetail(String companyStaffId) {
        Log.e(TAG, "getStaffDetail : " + companyStaffId);
        UserInfo userInfo = PrefKit.getUserInfo(getContext());
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_STAFF_ID", companyStaffId);
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        new TelKit(getContext(), this).request(TelKit.URL_API_OCR_STAFF_DETAIL, body);
    }

    private void showManagerCompleteDialog(JSONObject data) {
        Log.e(TAG, "showManagerCompleteDialog data : " + data);
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_manager_complete);

        // 바깥 투명 처리
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCancelable(false); // 바깥 터치 막기 (원하면 true)

        // ⭐ 90% width 설정
        dialog.show(); // show() 이후에 width 설정해야 적용됨

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = (int) (dm.widthPixels * 0.9); // 90%
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // View 연결
        TextView txtCompany = dialog.findViewById(R.id.txtCompany);
        TextView txtName = dialog.findViewById(R.id.txtName);
        TextView txtPhone = dialog.findViewById(R.id.txtPhone);
        TextView txtTel = dialog.findViewById(R.id.txtTel);
        TextView txtEmail = dialog.findViewById(R.id.txtEmail);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        LinearLayout staff_error_layout = dialog.findViewById(R.id.staff_error_layout);

        // 값 세팅 (동적으로 바꿀 수 있음)
        txtCompany.setText(data.optString("COMPANY_NAME"));
        String name = data.optString("STAFF_NAME");
        String dept = data.optString("STAFF_DEPT");
        String position = data.optString("STAFF_POSITION");

        StringBuilder sb = new StringBuilder();
        sb.append(name);

        if (!dept.isEmpty()) {
            sb.append("   ").append(dept);
        }

        if (!position.isEmpty()) {
            sb.append(" | ").append(position);
        }

        txtName.setText(sb.toString());

        txtPhone.setText(data.optString("STAFF_MOBILE"));
        txtTel.setText(data.optString("STAFF_PHONE"));
        txtEmail.setText(data.optString("STAFF_EMAIL"));

        CompanyInfo companyInfo_new = new CompanyInfo();
        companyInfo_new.COMPANY_ID = Integer.parseInt(data.optString("COMPANY_ID"));
        companyInfo_new.COMPANY_NAME = data.optString("COMPANY_NAME");

        if (data.optString("WORK_FLAG").equals("N")) {
            staff_error_layout.setVisibility(VISIBLE);
        } else {
            staff_error_layout.setVisibility(GONE);
        }
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Log.e(TAG, "COMPANY_STAFF_ID: " + data.optString("COMPANY_STAFF_ID"));
            if (data.optString("WORK_FLAG").equals("N")) {
                Intent intent = new Intent(getContext(), NameCardViewActivity.class);
                intent.putExtra("COMPANY_STAFF_ID", data.optString("COMPANY_STAFF_ID"));
                intent.putExtra("WORK_FLAG", "N");
                intent.putExtra("EDIT_TYPE", "1");
                startActivity(intent);
                Kit.ActivityManager.finishActivity(NameCardSearchActivity.class);
                getActivity().finish();
            } else {
                Intent intent = new Intent(getContext(), RegSalesActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo_new);
                startActivity(intent);
                Kit.ActivityManager.finishActivity(NameCardSearchActivity.class);
                getActivity().finish();
            }
        });
    }
}

