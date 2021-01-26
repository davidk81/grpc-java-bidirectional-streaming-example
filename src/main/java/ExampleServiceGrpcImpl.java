import com.example.BiDirectionalExampleService.FooRequest;
import com.example.BiDirectionalExampleService.FooResponse;
import com.example.ExampleServiceGrpc.ExampleServiceImplBase;
import io.grpc.stub.StreamObserver;

public class ExampleServiceGrpcImpl extends ExampleServiceImplBase {
    @Override
    public void foo(FooRequest request, StreamObserver<FooResponse> responseObserver) {
        System.out.println("foo called: \n" + request);
        FooResponse response = FooResponse.newBuilder().setResult(request.getAge()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
