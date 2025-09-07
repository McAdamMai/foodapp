package com.foodapp.price_reader.domain.service;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import com.foodapp.price_reader.persistence.repository.MerchandisePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class AdminRestfulService {
    private final MerchandisePriceRepository merchandiseRepo;
    public MerchandisePrice savePrice(MerchandisePrice mp) {
        return merchandiseRepo.save(mp);
    }

    public Optional<MerchandisePrice> findById(Long id) {
        return merchandiseRepo.findById(id);
    }

    public List<MerchandisePrice> findAll() {
        return merchandiseRepo.findAll();
    }

}
