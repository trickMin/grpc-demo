package org.hango.clound.test;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import test.hello.HelloProto;
import test.hello.HelloGrpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HelloServer {

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new HelloIml())  //这里可以添加多个模块
                .build()
                .start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    HelloServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloServer server = new HelloServer();
        server.start();
        server.blockUntilShutdown();
    }

    private static class HelloIml extends HelloGrpc.HelloImplBase {
        /**
         * 简单RPC方法实现
         * @param request 请求参数
         * @param responseObserver 请求参数监听器
         */
        @Override
        public void sayHello(HelloProto.HelloRequest request, StreamObserver<HelloProto.HelloResponse> responseObserver) {
            // super.sayHello(request, responseObserver);
            HelloProto.HelloResponse helloResponse = HelloProto.HelloResponse.newBuilder().setMessage("Hello " + request.getName() + ", I'm Java grpc Server").build();
            responseObserver.onNext(helloResponse);
            responseObserver.onCompleted();
        }


        /**
         * 双端流式RPC方法实现
         * @param responseObserver 响应监听器
         * @return 请求监听器
         */
        @Override
        public StreamObserver<HelloProto.HelloRequest> sayHelloStream(final StreamObserver<HelloProto.HelloResponse> responseObserver) {
            return new StreamObserver<HelloProto.HelloRequest>() {

                public void onNext(HelloProto.HelloRequest helloRequest) {
                    System.out.println("服务端收到: " + helloRequest.getName());
                    HelloProto.HelloResponse helloResponse = HelloProto.HelloResponse.newBuilder().setMessage("Hello " + helloRequest.getName() + ", I'm Java grpc Server").build();
                    responseObserver.onNext(helloResponse);
                }

                public void onError(Throwable throwable) {
                    System.out.println("错误");
                }

                public void onCompleted() {
                    System.out.println("结束");
                    responseObserver.onCompleted();
                }
            };
        }
    }
}