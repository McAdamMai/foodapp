package com.foodapp.base_price_manager.application;

import com.foodapp.base_price_manager.domain.fsm.BasePriceStatus;
import com.foodapp.base_price_manager.infrastructure.audit.AuditService;
import com.foodapp.base_price_manager.infrastructure.persistence.entity.BasePriceChangeRequestEntity;
import com.foodapp.base_price_manager.infrastructure.persistence.repository.BasePriceChangeRequestWriteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BasePriceRequestService {
    private final BasePriceChangeRequestWriteRepository writeRepository;
    private final AuditService auditService;

    public BasePriceRequestService( BasePriceChangeRequestWriteRepository writeRepository
                                    ,AuditService auditService
    ){
        this.writeRepository = writeRepository;
        this.auditService = auditService;
    }

    public void approved(Long id, String operator){
        BasePriceChangeRequestEntity entity = writeRepository.getOrThrow(id);
        String beforejson = null;
        entity.setStatus(BasePriceStatus.APPROVED);
        BasePriceChangeRequestEntity saved = writeRepository.save(entity);
        String afterjson = null;
        auditService.recordBasePriceChangeRequest(
                saved.getId(),
                saved.getVersion(),
                "TRANSITION",
                operator,
                null,
                "APPROVE",
                beforejson,
                afterjson

        );


    }


}
