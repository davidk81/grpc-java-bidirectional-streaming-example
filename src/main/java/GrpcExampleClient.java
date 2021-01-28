import com.example.BiDirectionalExampleService.FooRequest;
import com.example.BiDirectionalExampleService.FooResponse;
import com.example.ExampleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.io.IOException;

public class GrpcExampleClient {

    public static void main(String [] args) throws IOException, InterruptedException {
        String host = "localhost";
        int port = 50000;
        System.out.printf("dialing %s:%d%n", host, port);
        ManagedChannel channel = NettyChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        // other stub types available, eg: ExampleServiceGrpc.ExampleServiceFutureStub
        ExampleServiceGrpc.ExampleServiceBlockingStub service = ExampleServiceGrpc.newBlockingStub(channel);

        // call method
        FooRequest req = FooRequest.newBuilder().setName("hello").setAge(2).build();
        FooResponse res = service.foo(req);
        System.out.println("got response: " + res.getResult());
    }

}
