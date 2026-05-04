package net.e_sang.fmsmobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.TelKit;

import java.util.Arrays;
import java.util.List;

public class VisitorStatusTabDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private String FAIR_ID = "";
    private String FAIR_DATE = "";
    private String FAIR_NAME = "";

    private VisitorStatusViewPager2Adapter visitorStatusViewPager2Adapter = null;
    private ViewPager2 viewPager2 = null;

    final List<String> tabTitle = Arrays.asList("현재전시 현황", "전시별 비교");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_D9CCB6);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_status_tab_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_D9CCB6));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_visitor_status_detail);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        visitorStatusViewPager2Adapter = new VisitorStatusViewPager2Adapter(getSupportFragmentManager(), getLifecycle());
        viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setAdapter(visitorStatusViewPager2Adapter);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            FAIR_DATE = intent.getExtras().getString("FAIR_DATE");
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
        }

        //=== TabLayout기능 추가 부분 ============================================
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabTitle.get(position));
            }
        }).attach();
        //========================================================================
    }


    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {

        } else {
            Toast.makeText(VisitorStatusTabDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public class VisitorStatusViewPager2Adapter extends FragmentStateAdapter {
        public VisitorStatusViewPager2Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return VisitorStatusDetailFragment.newInstance(FAIR_NAME, FAIR_DATE, FAIR_ID);
                case 1:
                    return VisitorStatusCompareFragment.newInstance(FAIR_NAME, FAIR_DATE, FAIR_ID);
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}