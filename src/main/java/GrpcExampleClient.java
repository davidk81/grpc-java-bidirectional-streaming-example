import com.example.BiDirectionalExampleService;
import com.example.BiDirectionalExampleService.RequestCall;
import com.example.ExampleServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcExampleClient {

    static long startTime = 0;
    static long totalSentBytes = 0;

    public static void main(String [] args) throws IOException, InterruptedException {
        String host = System.getProperty("host", "localhost");
        int port = Integer.parseInt(System.getProperty("port", "50000"));
        System.out.printf("dialing %s:%d%n", host, port);
        ManagedChannel channel = NettyChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(8000000)
                .initialFlowControlWindow(8000000)
                .flowControlWindow(8000000)
                .build();
        ExampleServiceGrpc.ExampleServiceStub service = ExampleServiceGrpc
                .newStub(channel)
                .withMaxOutboundMessageSize(8000000)
                .withMaxInboundMessageSize(8000000);
        CountDownLatch finishedLatch = new CountDownLatch(2);

        StreamObserver<BiDirectionalExampleService.ResponseCall> sharedObs = new StreamObserver<BiDirectionalExampleService.ResponseCall>() {
            long totalRecvBytes = 0;
            @Override
            public void onNext(BiDirectionalExampleService.ResponseCall value) {
                totalRecvBytes += value.getSize();
                double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                System.out.printf(
                        "stats: size: %s, rate: %sbps%n",
                        humanReadableByteCountBin(totalRecvBytes),
                        humanReadableCountSI((long) (totalRecvBytes * 8 / elapsed)));
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on error");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                System.out.printf(
                        "stats: size: %s, rate: %sbps%n",
                        humanReadableByteCountBin(totalRecvBytes),
                        humanReadableCountSI((long) (totalRecvBytes * 8 / elapsed)));
                System.out.println("on completed");
                finishedLatch.countDown();
            }
        };

        StreamObserver<com.example.BiDirectionalExampleService.RequestCall> observer = service.connect(sharedObs);
        ExecutorService es = Executors.newFixedThreadPool(1);
        startTime = System.currentTimeMillis();
        byte[] b = new byte[1000000];
        new Random().nextBytes(b);
        ByteString data = ByteString.copyFrom(b);
        while (totalSentBytes < 1e9) {
            es.submit(() -> {
                RequestCall req = RequestCall.newBuilder().setData(data).build();
                observer.onNext(req);
            });
            totalSentBytes += b.length;
        }
        finishedLatch.await();
        observer.onCompleted();
    }

    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static String humanReadableCountSI(long units) {
        if (-1000 < units && units < 1000) {
            return units + " ";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (units <= -999_950 || units >= 999_950) {
            units /= 1000;
            ci.next();
        }
        return String.format("%.1f %c", units / 1000.0, ci.current());
    }

    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}
