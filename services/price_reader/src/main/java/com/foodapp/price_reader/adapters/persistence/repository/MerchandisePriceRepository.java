package com.foodapp.price_reader.adapters.persistence.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface MerchandisePriceRepository
        extends MongoRepository<MerchandisePrice, String>{

    @Query("{'merchandiseUuid' : ?0, 'validFrom' : {$lte : ?1}, 'validTo' : {$gte : ?2} }")
    //$lte = less than or equal
    Optional<MerchandisePrice> findByValidPriceForInstant(String merchandiseUuid, Instant at);
}
