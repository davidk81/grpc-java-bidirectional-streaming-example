import com.example.BiDirectionalExampleService;
import com.example.BiDirectionalExampleService.ResponseCall;
import com.example.ExampleServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ExampleServiceGrpcImpl extends ExampleServiceGrpc.ExampleServiceImplBase {
    @Override
    public StreamObserver<BiDirectionalExampleService.RequestCall> connect(StreamObserver<BiDirectionalExampleService.ResponseCall> responseObserver) {
        System.out.println("Connecting stream observer");
        StreamObserver<BiDirectionalExampleService.RequestCall> so = new StreamObserver<BiDirectionalExampleService.RequestCall>() {
            @Override
            public void onNext(BiDirectionalExampleService.RequestCall value) {
                System.out.println("onNext from server");
                ResponseCall res = ResponseCall.newBuilder().setSize(value.getData().size()).build();
                responseObserver.onNext(res);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed");
            }
        };
        return so;
    }
}
