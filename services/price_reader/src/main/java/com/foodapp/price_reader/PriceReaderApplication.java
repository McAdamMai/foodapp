package com.foodapp.price_reader;

import io.grpc.Grpc;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		exclude = {
				net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration.class,

		}
)
public class PriceReaderApplication {
	public static void main(String[] args) {
		SpringApplication.run(PriceReaderApplication.class, args);
	}
}
