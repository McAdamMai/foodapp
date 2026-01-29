package com.foodapp.base_price_manager;

import com.foodapp.base_price_manager.application.BasePriceRequestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevApproveRunner implements CommandLineRunner {

    private final BasePriceRequestService service;

    public DevApproveRunner(BasePriceRequestService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        service.approved(1L, "dev_operator");
        System.out.println("✅ approved request 1");
    }
}
