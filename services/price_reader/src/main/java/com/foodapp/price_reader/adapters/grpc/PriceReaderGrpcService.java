package com.foodapp.price_reader.adapters.grpc;

import com.foodapp.contracts.price_reader.MerchandisePriceRequest;
import com.foodapp.contracts.price_reader.MerchandisePriceResponse;

import com.foodapp.contracts.price_reader.PriceReaderServiceGrpc;
import com.foodapp.price_reader.application.service.PriceReaderApplicationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class PriceReaderGrpcService extends PriceReaderServiceGrpc.PriceReaderServiceImplBase {

    private final PriceReaderApplicationService appService;

    @Override
    public  void getPrice(MerchandisePriceRequest request, StreamObserver<MerchandisePriceResponse> responseObserver){
        appService
                .findPrice(request.getMerchandiseUuid())
                .ifPresentOrElse(
                        price -> { //price is returned from findPrice, type:MerchandisePriceResponse(
                            responseObserver.onNext(price);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND.asException()) // return exception if empty price

                );
    }
}
