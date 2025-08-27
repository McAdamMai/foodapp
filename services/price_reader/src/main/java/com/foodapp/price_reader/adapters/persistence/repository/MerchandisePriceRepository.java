package com.foodapp.price_reader.adapters.persistence.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.foodapp.price_reader.domain.common.MerchandisePrice;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchandisePriceRepository
        extends MongoRepository<MerchandisePrice, String>{
}
