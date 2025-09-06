package com.foodapp.price_reader.adapters.persistence.repository;

import com.foodapp.price_reader.domain.common.MerchandisePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface MerchandisePriceRepository extends JpaRepository <MerchandisePrice, Long>{

    Optional<MerchandisePrice> findByMerchandiseUuidAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String merchandiseUuid,
            Instant validFrom,
            Instant validTo

    );


}