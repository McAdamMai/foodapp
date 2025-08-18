package com.foodapp.pricing.adapters.grpc;

import com.foodapp.contracts.pricing.MerchandisePriceRequest;
import com.foodapp.contracts.pricing.MerchandisePriceResponse;
import com.foodapp.contracts.pricing.PricingServiceGrpc;
import com.foodapp.pricing.application.service.PricingApplicationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    final private PricingApplicationService appService;

    @Override
    public  void getPrice(MerchandisePriceRequest request, StreamObserver<MerchandisePriceResponse> responseObserver){
        appService
                .findPrice(request.getMerchandiseUuid())
                .ifPresentOrElse(
                        price -> { //price is returned from findPrice(
                            responseObserver.onNext(price);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND.asException()) // return exception if empty price

                );
    }
}
