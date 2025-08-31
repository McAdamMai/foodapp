package com.foodapp.price_reader.adapters.grpc;

import com.foodapp.contracts.price_reader.MerchandisePriceRequest;
import com.foodapp.contracts.price_reader.MerchandisePriceResponse;

import com.foodapp.contracts.price_reader.PriceReaderServiceGrpc;
import com.foodapp.price_reader.application.service.PriceReaderApplicationService;
import com.google.protobuf.Timestamp;
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
        // Extract all necessary fields from request
        String merchandiseUuid = request.getMerchandiseUuid();
        String currency = request.getCurrency().isEmpty()? "CAD" : request.getCurrency();

        if(!request.hasAt()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("a timestamp is required").asException());
        } // time is essential
        Timestamp at = request.getAt();
        // provide fallback and default value for optional parameters
        appService
                .findPrice(merchandiseUuid, currency, at)
                .ifPresentOrElse(
                        price -> { //price is returned from findPrice, type:MerchandisePriceResponse(
                            responseObserver.onNext(price);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND.asException()) // return exception if empty price

                );
    }
}
// PriceReaderGrpcService class is annotated with @GrpcService,
// which marks it as a gRPC service that will be registered and exposed by the Netty-based gRPC server in the Spring Boot application
// this Unary RPC (Request-Response) means one request one response
// Server-Streaming RPC means One request → Multiple responses (streamed from server to client).
// Client-Streaming RPC means Multiple requests → One response.
// Multiple requests ↔ Multiple responses (a two-way stream).