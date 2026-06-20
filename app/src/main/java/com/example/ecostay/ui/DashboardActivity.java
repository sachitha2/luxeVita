package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.SlideshowAdapter;
import com.example.ecostay.ui.viewmodel.DashboardViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final long SLIDE_INTERVAL_MS = 4000L;

    private DashboardViewModel dashboardViewModel;
    private View slideshowContainer;
    private ViewPager2 viewPagerSlideshow;
    private TabLayout tabSlideshowDots;
    private SlideshowAdapter slideshowAdapter;
    private TabLayoutMediator tabLayoutMediator;
    private final Handler slideHandler = new Handler(Looper.getMainLooper());
    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerSlideshow == null || slideshowAdapter.getItemCount() <= 1) {
                return;
            }
            int next = (viewPagerSlideshow.getCurrentItem() + 1) % slideshowAdapter.getItemCount();
            viewPagerSlideshow.setCurrentItem(next, true);
            slideHandler.postDelayed(this, SLIDE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (SessionManager.isAdmin(this)) {
            startActivity(new Intent(this, com.example.ecostay.ui.admin.AdminDashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        String name = SessionManager.getUserName(this);
        tvGreeting.setText(getString(R.string.dashboard_greeting, name));

        slideshowContainer = findViewById(R.id.slideshowContainer);
        viewPagerSlideshow = findViewById(R.id.viewPagerSlideshow);
        tabSlideshowDots = findViewById(R.id.tabSlideshowDots);
        slideshowAdapter = new SlideshowAdapter();
        viewPagerSlideshow.setAdapter(slideshowAdapter);
        viewPagerSlideshow.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                resetAutoScroll();
            }
        });

        dashboardViewModel.getActiveSlides().observe(this, this::showSlideshow);

        setupNavCard(R.id.cardBrowseServices, R.id.navBrowseServices,
                R.drawable.ic_nav_services, R.drawable.bg_nav_icon_services,
                R.string.card_browse_services, R.string.card_browse_services_desc,
                BrowseServicesActivity.class);
        setupNavCard(R.id.cardMyBookings, R.id.navMyBookings,
                R.drawable.ic_nav_bookings, R.drawable.bg_nav_icon_bookings,
                R.string.card_my_bookings, R.string.card_my_bookings_desc,
                MyBookingsActivity.class);
        setupNavCard(R.id.cardSavedDevices, R.id.navSavedDevices,
                R.drawable.ic_nav_devices, R.drawable.bg_nav_icon_devices,
                R.string.card_saved_devices, R.string.card_saved_devices_desc,
                DeviceListActivity.class);
        setupNavCard(R.id.cardFaqsTips, R.id.navFaqsTips,
                R.drawable.ic_nav_faq, R.drawable.bg_nav_icon_faq,
                R.string.card_faqs_tips, R.string.card_faqs_tips_desc,
                FaqTipsActivity.class);
        setupNavCard(R.id.cardSupport, R.id.navSupport,
                R.drawable.ic_nav_support, R.drawable.bg_nav_icon_support,
                R.string.card_support, R.string.card_support_desc,
                SupportActivity.class);
        setupNavCard(R.id.cardMyProfile, R.id.navMyProfile,
                R.drawable.ic_nav_profile, R.drawable.bg_nav_icon_profile,
                R.string.card_my_profile, R.string.card_my_profile_desc,
                EditProfileActivity.class);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dashboardViewModel.loadActiveSlides();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardViewModel != null) {
            dashboardViewModel.loadActiveSlides();
        }
        startAutoScroll();
    }

    @Override
    protected void onPause() {
        stopAutoScroll();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
        stopAutoScroll();
        super.onDestroy();
    }

    private void showSlideshow(List<SlideshowSlideEntity> slides) {
        boolean hasSlides = slides != null && !slides.isEmpty();
        slideshowContainer.setVisibility(hasSlides ? View.VISIBLE : View.GONE);
        if (!hasSlides) {
            stopAutoScroll();
            return;
        }

        slideshowAdapter.setSlides(slides);
        attachTabDots(slides.size());
        viewPagerSlideshow.setCurrentItem(0, false);
        resetAutoScroll();
    }

    private void attachTabDots(int count) {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
        tabSlideshowDots.removeAllTabs();
        if (count <= 1) {
            tabSlideshowDots.setVisibility(View.GONE);
        } else {
            tabSlideshowDots.setVisibility(View.VISIBLE);
            tabLayoutMediator = new TabLayoutMediator(tabSlideshowDots, viewPagerSlideshow,
                    (tab, position) -> { });
            tabLayoutMediator.attach();
        }
    }

    private void startAutoScroll() {
        if (slideshowAdapter == null || slideshowAdapter.getItemCount() <= 1) {
            return;
        }
        slideHandler.removeCallbacks(slideRunnable);
        slideHandler.postDelayed(slideRunnable, SLIDE_INTERVAL_MS);
    }

    private void resetAutoScroll() {
        stopAutoScroll();
        startAutoScroll();
    }

    private void stopAutoScroll() {
        slideHandler.removeCallbacks(slideRunnable);
    }

    private void setupNavCard(int cardId, int contentRootId, int iconRes, int iconBgRes,
                              int titleRes, int descRes, Class<?> targetActivity) {
        findViewById(cardId).setOnClickListener(v ->
                startActivity(new Intent(this, targetActivity)));

        android.view.View content = findViewById(contentRootId);
        android.view.View iconContainer = content.findViewById(R.id.iconContainer);
        ImageView icon = content.findViewById(R.id.ivNavIcon);
        TextView title = content.findViewById(R.id.tvNavTitle);
        TextView desc = content.findViewById(R.id.tvNavDesc);
        iconContainer.setBackgroundResource(iconBgRes);
        icon.setImageResource(iconRes);
        title.setText(titleRes);
        desc.setText(descRes);
    }
}
