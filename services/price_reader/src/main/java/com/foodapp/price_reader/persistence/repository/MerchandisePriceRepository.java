package com.foodapp.price_reader.persistence.repository;

import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface MerchandisePriceRepository extends JpaRepository <MerchandisePrice, Long>{

    @Query("SELECT mp FROM MerchandisePrice mp " +
            "WHERE mp.merchandiseUuid = :merchandiseUuid " +
            "AND mp.validFrom <= :atInstant " +
            "AND (mp.validTo >= :atInstant OR mp.validTo IS NULL)")
    Optional<MerchandisePrice> findByValidPriceForInstant(
            @Param("merchandiseUuid") String merchandiseUuid,
            @Param("atInstant") Instant atInstant
    );


}