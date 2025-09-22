package com.foodapp.price_reader.adapters.grpc;

import com.foodapp.contracts.price_reader.v1.*;
import com.foodapp.price_reader.domain.service.PriceQueryService;
import com.foodapp.price_reader.domain.service.TimelineService;
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
    private final PriceQueryService priceQueryService;
    private final TimelineService timelineService;

    @Override
    public void getPrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver){
        if(!request.hasAt()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("a timestamp is required").asException());
            return;
        }
        try {
            System.out.println("Received request for skuID: " + request.getSkuId());
            Optional<PriceResponse> response = priceQueryService.getPrice(
                            request.getSkuId(),
                            timeStampTranslator(request.getAt()))
                    .map(grpcMapper::toProto);

            if(response.isPresent()){
                System.out.println(String.format("Sending request for skuID: %s at timestamp: %s", request.getSkuId(), timeStampTranslator(request.getAt())));
                responseObserver.onNext(response.get());
            } else {
                System.out.println(String.format("No price found for skuID: %s at timestamp: %s", request.getSkuId(), timeStampTranslator(request.getAt())));
                responseObserver.onError(Status.NOT_FOUND.asException());
                return;
            }
            // onCompleted and onError cannot exist in on run, every onError should call return
            System.out.println("Call completed for skuID: " + request.getSkuId());
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.out.println("Error occurs while processing: " + e.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
        }
    }

    @Override
    public void getTimeline(TimelineRequest request, StreamObserver<TimelineResponse> responseObserver) {
        if(!request.hasKey()){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("a key is required").asException());
            return;
        }
        try {
            // call the timeline service to get the domain data
            var domainIntervals = timelineService.getTimeline(
                    grpcMapper.toDomain(request.getKey()),
                    timeStampTranslator(request.getFrom()),
                    timeStampTranslator(request.getTo()),
                    request.getLimit());

            // convert to response
            TimelineResponse response = grpcMapper.toTimelineResponseProto(domainIntervals, request.getLimit());

            // send response to client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch (Exception e){
            //handle exception and send error to the client
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
        }
    }

    private Instant timeStampTranslator(Timestamp at){
        System.out.println("Converting timestamp : " + at );
        return Instant.ofEpochSecond(at.getSeconds(), at.getNanos());
    }

}
// PriceReaderGrpcService class is annotated with @GrpcService,
// which marks it as a gRPC service that will be registered and exposed by the Netty-based gRPC server in the Spring Boot application
// this Unary RPC (Request-Response) means one request one response
// Server-Streaming RPC means One request → Multiple responses (streamed from server to client).
// Client-Streaming RPC means Multiple requests → One response.
// Multiple requests ↔ Multiple responses (a two-way stream).