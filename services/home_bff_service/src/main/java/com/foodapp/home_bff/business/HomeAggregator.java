package com.foodapp.home_bff.business;

import com.foodapp.home_bff.utils.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class HomeAggregator {

    @Qualifier("ioPool")
    private final Executor ioPool;

    public CompletableFuture<String> fetchItem(String skuId, EnumSet<Section> sections) {
        String ret = "Hello World";
        return CompletableFuture.completedFuture(ret);
    }
}
