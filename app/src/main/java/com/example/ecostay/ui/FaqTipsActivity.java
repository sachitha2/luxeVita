package com.example.ecostay.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.FaqAdapter;
import com.example.ecostay.ui.adapters.MaintenanceTipAdapter;
import com.example.ecostay.ui.viewmodel.FaqTipsViewModel;
import com.example.ecostay.util.ToolbarUtils;

import java.util.List;

public class FaqTipsActivity extends AppCompatActivity {

    private RecyclerView rvFaqs;
    private RecyclerView rvTips;
    private TextView tvFaqCount;
    private TextView tvTipsCount;
    private View ivFaqEmpty;
    private View tvFaqEmpty;
    private View ivTipsEmpty;
    private View tvTipsEmpty;

    private FaqAdapter faqAdapter;
    private MaintenanceTipAdapter tipAdapter;

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

        rvFaqs = findViewById(R.id.rvFaqs);
        rvTips = findViewById(R.id.rvTips);
        tvFaqCount = findViewById(R.id.tvFaqCount);
        tvTipsCount = findViewById(R.id.tvTipsCount);
        ivFaqEmpty = findViewById(R.id.ivFaqEmpty);
        tvFaqEmpty = findViewById(R.id.tvFaqEmpty);
        ivTipsEmpty = findViewById(R.id.ivTipsEmpty);
        tvTipsEmpty = findViewById(R.id.tvTipsEmpty);

        faqAdapter = new FaqAdapter();
        tipAdapter = new MaintenanceTipAdapter();

        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        rvFaqs.setAdapter(faqAdapter);
        rvFaqs.setNestedScrollingEnabled(false);

        rvTips.setLayoutManager(new LinearLayoutManager(this));
        rvTips.setAdapter(tipAdapter);
        rvTips.setNestedScrollingEnabled(false);

        viewModel.getFaqs().observe(this, this::updateFaqSection);
        viewModel.getTips().observe(this, this::updateTipsSection);

        viewModel.loadContent();
    }

    private void updateFaqSection(List<FaqEntity> faqs) {
        faqAdapter.setItems(faqs);
        int count = faqs != null ? faqs.size() : 0;
        boolean empty = count == 0;
        int emptyVisibility = empty ? View.VISIBLE : View.GONE;
        ivFaqEmpty.setVisibility(emptyVisibility);
        tvFaqEmpty.setVisibility(emptyVisibility);
        rvFaqs.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            tvFaqCount.setVisibility(View.GONE);
            return;
        }
        tvFaqCount.setText(getString(R.string.faq_count, count));
        tvFaqCount.setVisibility(View.VISIBLE);
    }

    private void updateTipsSection(List<MaintenanceTipEntity> tips) {
        tipAdapter.setItems(tips);
        int count = tips != null ? tips.size() : 0;
        boolean empty = count == 0;
        int emptyVisibility = empty ? View.VISIBLE : View.GONE;
        ivTipsEmpty.setVisibility(emptyVisibility);
        tvTipsEmpty.setVisibility(emptyVisibility);
        rvTips.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            tvTipsCount.setVisibility(View.GONE);
            return;
        }
        tvTipsCount.setText(getString(R.string.tips_count, count));
        tvTipsCount.setVisibility(View.VISIBLE);
    }
}
