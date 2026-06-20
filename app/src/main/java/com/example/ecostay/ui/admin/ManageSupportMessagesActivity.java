package com.example.ecostay.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.AdminSupportMessageSummary;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminSupportMessageAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminSupportViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class ManageSupportMessagesActivity extends AppCompatActivity {

    private AdminSupportViewModel supportViewModel;
    private AdminSupportMessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_support_messages);
        ToolbarUtils.setupBackToolbar(this, R.string.manage_support_title);
        supportViewModel = new ViewModelProvider(this).get(AdminSupportViewModel.class);

        RecyclerView rvItems = findViewById(R.id.rvItems);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AdminSupportMessageAdapter(this::showMessageDetails);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        supportViewModel.getMessages().observe(this, messages -> {
            adapter.setItems(messages);
            boolean empty = messages == null || messages.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        supportViewModel.loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportViewModel.loadMessages();
    }

    private void showMessageDetails(AdminSupportMessageSummary message) {
        String details = getString(R.string.label_message_from, message.userName) + "\n"
                + getString(R.string.label_message_date, message.createdAt) + "\n"
                + getString(R.string.label_contact_email, message.userEmail) + "\n"
                + getString(R.string.label_contact_phone, message.userPhone) + "\n\n"
                + message.message;

        new AlertDialog.Builder(this)
                .setTitle(R.string.support_message_details_title)
                .setMessage(details)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
