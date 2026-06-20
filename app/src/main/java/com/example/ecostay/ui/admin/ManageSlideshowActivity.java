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
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminSlideshowAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminSlideshowViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class ManageSlideshowActivity extends AppCompatActivity {

    private AdminSlideshowViewModel slideshowViewModel;
    private AdminSlideshowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_slideshow);
        ToolbarUtils.setupBackToolbar(this, R.string.manage_slideshow_title);
        slideshowViewModel = new ViewModelProvider(this).get(AdminSlideshowViewModel.class);

        Button btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvItems = findViewById(R.id.rvItems);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AdminSlideshowAdapter(new AdminSlideshowAdapter.Listener() {
            @Override
            public void onEdit(SlideshowSlideEntity slide) {
                Intent intent = new Intent(ManageSlideshowActivity.this, AddEditSlideActivity.class);
                intent.putExtra(AddEditSlideActivity.EXTRA_SLIDE_ID, slide.slideId);
                startActivity(intent);
            }

            @Override
            public void onDelete(SlideshowSlideEntity slide) {
                new AlertDialog.Builder(ManageSlideshowActivity.this)
                        .setMessage(R.string.delete_slide_confirm)
                        .setPositiveButton(R.string.action_delete, (d, w) ->
                                slideshowViewModel.deleteSlide(slide))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditSlideActivity.class)));

        slideshowViewModel.getSlides().observe(this, slides -> {
            adapter.setItems(slides);
            boolean empty = slides == null || slides.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        slideshowViewModel.getDeleteResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.slide_deleted, Toast.LENGTH_SHORT).show();
            }
        });

        slideshowViewModel.loadSlides();
    }

    @Override
    protected void onResume() {
        super.onResume();
        slideshowViewModel.loadSlides();
    }
}
