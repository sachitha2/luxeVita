package com.example.ecostay.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminFaqAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminFaqViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class ManageFaqActivity extends AppCompatActivity {

    private AdminFaqViewModel faqViewModel;
    private AdminFaqAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_faq);
        ToolbarUtils.setupBackToolbar(this, R.string.manage_faq_title);
        faqViewModel = new ViewModelProvider(this).get(AdminFaqViewModel.class);

        Button btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvItems = findViewById(R.id.rvItems);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AdminFaqAdapter(new AdminFaqAdapter.Listener() {
            @Override
            public void onEdit(FaqEntity faq) {
                Intent intent = new Intent(ManageFaqActivity.this, AddEditFaqActivity.class);
                intent.putExtra(AddEditFaqActivity.EXTRA_FAQ_ID, faq.faqId);
                startActivity(intent);
            }

            @Override
            public void onDelete(FaqEntity faq) {
                new AlertDialog.Builder(ManageFaqActivity.this)
                        .setMessage(R.string.delete_faq_confirm)
                        .setPositiveButton(R.string.action_delete, (d, w) ->
                                faqViewModel.deleteFaq(faq))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditFaqActivity.class)));

        faqViewModel.getFaqs().observe(this, faqs -> {
            adapter.setItems(faqs);
            boolean empty = faqs == null || faqs.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        faqViewModel.getDeleteResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.faq_deleted, Toast.LENGTH_SHORT).show();
            }
        });

        faqViewModel.loadFaqs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faqViewModel.loadFaqs();
    }
}
