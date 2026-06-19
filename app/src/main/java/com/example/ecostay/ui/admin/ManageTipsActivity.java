package com.example.ecostay.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminTipAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminTipViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class ManageTipsActivity extends AppCompatActivity {

    private AdminTipViewModel tipViewModel;
    private AdminTipAdapter adapter;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_tips);
        ToolbarUtils.setupBackToolbar(this, R.string.manage_tips_title);
        tipViewModel = new ViewModelProvider(this).get(AdminTipViewModel.class);

        Spinner spDeviceFilter = findViewById(R.id.spDeviceFilter);
        Button btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvItems = findViewById(R.id.rvItems);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_categories, R.layout.item_spinner_category);
        filterAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spDeviceFilter.setAdapter(filterAdapter);

        adapter = new AdminTipAdapter(new AdminTipAdapter.Listener() {
            @Override
            public void onEdit(MaintenanceTipEntity tip) {
                Intent intent = new Intent(ManageTipsActivity.this, AddEditTipActivity.class);
                intent.putExtra(AddEditTipActivity.EXTRA_TIP_ID, tip.tipId);
                startActivity(intent);
            }

            @Override
            public void onDelete(MaintenanceTipEntity tip) {
                new AlertDialog.Builder(ManageTipsActivity.this)
                        .setMessage(R.string.delete_tip_confirm)
                        .setPositiveButton(R.string.action_delete, (d, w) ->
                                tipViewModel.deleteTip(tip))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        spDeviceFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                tipViewModel.loadTipsByDeviceType(currentFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditTipActivity.class)));

        tipViewModel.getTips().observe(this, tips -> {
            adapter.setItems(tips);
            boolean empty = tips == null || tips.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        tipViewModel.getDeleteResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.tip_deleted, Toast.LENGTH_SHORT).show();
            }
        });

        tipViewModel.loadTipsByDeviceType(currentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tipViewModel.loadTipsByDeviceType(currentFilter);
    }
}
