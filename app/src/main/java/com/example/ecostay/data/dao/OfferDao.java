package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ecostay.data.entity.OfferEntity;

import java.util.List;

@Dao
public interface OfferDao {

    @Query(
            "SELECT * FROM offers " +
                    "WHERE valid_from_epoch_day <= :epochDay AND valid_to_epoch_day >= :epochDay " +
                    "ORDER BY valid_to_epoch_day ASC"
    )
    List<OfferEntity> getActiveOffers(long epochDay);

    @Query("SELECT * FROM offers ORDER BY valid_to_epoch_day ASC")
    List<OfferEntity> getAll();

    @Insert
    long[] insertAll(OfferEntity... offers);
}

