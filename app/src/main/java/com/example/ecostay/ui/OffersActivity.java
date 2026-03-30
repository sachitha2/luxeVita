package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.AttractionDao;
import com.example.ecostay.data.dao.OfferDao;
import com.example.ecostay.data.entity.AttractionEntity;
import com.example.ecostay.data.entity.OfferEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.AttractionAdapter;
import com.example.ecostay.ui.adapters.OfferAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OffersActivity extends AppCompatActivity {

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private RecyclerView rvOffers;
    private RecyclerView rvAttractions;
    private OfferAdapter offerAdapter;
    private AttractionAdapter attractionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        OfferDao offerDao = database.offerDao();
        AttractionDao attractionDao = database.attractionDao();

        rvOffers = findViewById(R.id.rvOffers);
        rvAttractions = findViewById(R.id.rvAttractions);

        offerAdapter = new OfferAdapter();
        attractionAdapter = new AttractionAdapter();

        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        rvOffers.setAdapter(offerAdapter);

        rvAttractions.setLayoutManager(new LinearLayoutManager(this));
        rvAttractions.setAdapter(attractionAdapter);

        dbExecutor.execute(() -> {
            long todayEpochDay = LocalDate.now().toEpochDay();
            List<OfferEntity> activeOffers = offerDao.getActiveOffers(todayEpochDay);
            List<AttractionEntity> attractions = attractionDao.getAll();

            runOnUiThread(() -> {
                offerAdapter.setItems(activeOffers == null ? new ArrayList<>() : activeOffers);
                attractionAdapter.setItems(attractions == null ? new ArrayList<>() : attractions);
            });
        });
    }
}

