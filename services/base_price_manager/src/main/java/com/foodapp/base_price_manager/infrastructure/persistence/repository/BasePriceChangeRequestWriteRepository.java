package com.foodapp.base_price_manager.infrastructure.persistence.repository;


import com.foodapp.base_price_manager.common.exception.NotFoundException;
import com.foodapp.base_price_manager.infrastructure.persistence.entity.BasePriceChangeRequestEntity;

import jakarta.persistence.OptimisticLockException;


import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;


@Repository
public class BasePriceChangeRequestWriteRepository {
    private  final BasePriceChangeRequestJpaRepository jpa;

    public  BasePriceChangeRequestWriteRepository(BasePriceChangeRequestJpaRepository jpa){
        this.jpa = jpa;

    }

    public BasePriceChangeRequestEntity getOrThrow(Long id){
        return jpa.findById(id)
                .orElseThrow (()->
                        new NotFoundException("BasePriceChangeRequest not found, id="+ id)
                );
    }

    public BasePriceChangeRequestEntity save(BasePriceChangeRequestEntity entity){
        try{
            return jpa.saveAndFlush(entity);
        }catch(ObjectOptimisticLockingFailureException
               | OptimisticLockException e){
            throw new com.foodapp.base_price_manager.common.exception.OptimisticLockException(
                    "Optimistic lock conflict for BasePriceChangeRequest, id="+ entity.getId()
                            + ",version="
                            + entity.getVersion(),
                    e
            );
        }
    }

}
