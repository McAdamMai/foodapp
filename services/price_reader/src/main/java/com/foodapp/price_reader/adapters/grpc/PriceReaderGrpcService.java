package com.foodapp.price_reader.adapters.grpc;

import com.foodapp.contracts.price_reader.v1.PriceRequest;
import com.foodapp.contracts.price_reader.v1.PriceResponse;
import com.foodapp.contracts.price_reader.v1.PriceServiceGrpc;
import com.foodapp.price_reader.domain.service.AdminRestfulService;

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
public class PriceReaderGrpcService extends PriceServiceGrpc.PriceServiceImplBase {


    private final PriceGrpcMapper grpcMapper;
    private final AdminRestfulService adminRestfulService;

    @Override
    public void getPrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver){
        if(!request.hasAt()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("a timestamp is required").asException());
            return;
        }

        try {
            Optional<PriceResponse> response = adminRestfulService.findPrice(
                            request.getSkuId(),
                            timeStampTranslator(request.getAt()))
                    .map(grpcMapper::toProto);

            if(response.isPresent()){
                responseObserver.onNext(response.get());
            } else {
                responseObserver.onError(Status.NOT_FOUND.asException());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
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