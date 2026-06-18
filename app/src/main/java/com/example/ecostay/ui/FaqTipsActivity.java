package com.example.ecostay.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.FaqAdapter;
import com.example.ecostay.ui.adapters.MaintenanceTipAdapter;
import com.example.ecostay.ui.viewmodel.FaqTipsViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class FaqTipsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_faq_tips);
        ToolbarUtils.setupBackToolbar(this, R.string.faq_tips_title);

        FaqTipsViewModel viewModel = new ViewModelProvider(this).get(FaqTipsViewModel.class);

        RecyclerView rvFaqs = findViewById(R.id.rvFaqs);
        RecyclerView rvTips = findViewById(R.id.rvTips);

        FaqAdapter faqAdapter = new FaqAdapter();
        MaintenanceTipAdapter tipAdapter = new MaintenanceTipAdapter();

        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        rvFaqs.setAdapter(faqAdapter);
        rvFaqs.setNestedScrollingEnabled(false);

        rvTips.setLayoutManager(new LinearLayoutManager(this));
        rvTips.setAdapter(tipAdapter);
        rvTips.setNestedScrollingEnabled(false);

        viewModel.getFaqs().observe(this, faqAdapter::setItems);
        viewModel.getTips().observe(this, tipAdapter::setItems);

        viewModel.loadContent();
    }
}
