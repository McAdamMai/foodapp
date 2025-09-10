package com.foodapp.price_reader.adapters.grpc;

import com.foodapp.contracts.price_reader.v1.MerchandisePriceRequest;
import com.foodapp.contracts.price_reader.v1.MerchandisePriceResponse;

import com.foodapp.contracts.price_reader.v1.PriceReaderServiceGrpc;
import com.foodapp.price_reader.domain.service.AdminRestfulService;
import com.foodapp.price_reader.domain.service.PriceQueryService;
import com.foodapp.price_reader.mapper.PriceGrpcMapper;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class PriceReaderGrpcService extends PriceReaderServiceGrpc.PriceReaderServiceImplBase {

    private final PriceQueryService priceQueryService;
    private final PriceGrpcMapper grpcMapper;
    private final AdminRestfulService adminRestfulService;

    @Override
    public void findPrice(MerchandisePriceRequest request, StreamObserver<MerchandisePriceResponse> responseObserver){
        if(!request.hasAt()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("a timestamp is required").asException());
        } // time is essential
        try{
            Optional<MerchandisePriceResponse> response = adminRestfulService.findPrice(
                    request.getMerchandiseUuid(),
                    request.getCurrency(),
                    timeStampTranslator(request.getAt()))
                    .map(grpcMapper::toProto);

            if(response.isPresent()){
                responseObserver.onNext(response.get());
            }else {
                responseObserver.onError(Status.NOT_FOUND.asException());
            }

            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
        }
    }

    private Instant timeStampTranslator(Timestamp at){
        return Instant.ofEpochSecond(at.getSeconds(), at.getNanos());
    }
}
// PriceReaderGrpcService class is annotated with @GrpcService,
// which marks it as a gRPC service that will be registered and exposed by the Netty-based gRPC server in the Spring Boot application
// this Unary RPC (Request-Response) means one request one response
// Server-Streaming RPC means One request → Multiple responses (streamed from server to client).
// Client-Streaming RPC means Multiple requests → One response.
// Multiple requests ↔ Multiple responses (a two-way stream).