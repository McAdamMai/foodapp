package com.foodapp.pricing.adapters.persistence.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.foodapp.pricing.domain.models.MerchandisePrice;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchandisePriceRepository
        extends MongoRepository<MerchandisePrice, String>{
}
