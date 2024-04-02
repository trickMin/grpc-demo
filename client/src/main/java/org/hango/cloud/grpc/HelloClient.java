package org.hango.cloud.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import test.hello.HelloProto;
import test.hello.HelloGrpc;

import java.util.concurrent.TimeUnit;

/**
 * @author yutao
 */
public class HelloClient {

    /**
     * 远程连接管理器,管理连接的生命周期
     */
    private final ManagedChannel channel;

    /**
     * 简单RPC阻塞式客户端
     */
    private final HelloGrpc.HelloBlockingStub blockingStub;

    /**
     * 流式RPC客户端
     */
    private final HelloGrpc.HelloStub helloStub;

    public HelloClient(String host, int port) {
        //初始化连接
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        //初始化远程服务Stub
        blockingStub = HelloGrpc.newBlockingStub(channel);
        helloStub = HelloGrpc.newStub(channel);
    }

    public StreamObserver<HelloProto.HelloRequest> initHelloStreamClient() {
        return helloStub.sayHelloStream(new StreamObserver<HelloProto.HelloResponse>() {
            public void onNext(HelloProto.HelloResponse helloResponse) {
                System.out.println("客户端收到： " + helloResponse.getMessage());
            }

            public void onError(Throwable throwable) {
                System.out.println("错误");
            }

            public void onCompleted() {
                System.out.println("结束");
            }
        });
    }

    /**
     * 客户端演示流程无需关闭，因此暂时不用，但正常关闭需要调用
     *
     * @throws InterruptedException 中断异常
     */
    public void shutdown() throws InterruptedException {
        //关闭连接
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * @param name                       字符串入参
     * @param helloRequestStreamObserver 流式监听器，用于触发客户端请求
     * @return 返回值无意义，函数中进行打印
     */
    public String sayHelloStream(String name, StreamObserver<HelloProto.HelloRequest> helloRequestStreamObserver) {
        //构造服务调用参数对象
        HelloProto.HelloRequest request = HelloProto.HelloRequest.newBuilder().setName(name).build();
        // 触发客户端请求
        helloRequestStreamObserver.onNext(request);
        //返回值
        return null;
    }

    /**
     * 简单RPC调用示例
     *
     * @param name 字符串入参
     * @return 服务端返回字符串
     */
    public String sayHello(String name) {
        //构造服务调用参数对象
        HelloProto.HelloRequest request = HelloProto.HelloRequest.newBuilder().setName(name).build();
        //调用远程服务方法
        HelloProto.HelloResponse response = blockingStub.sayHello(request);
        //返回值
        return response.getMessage();
    }


    public static void main(String[] args) throws InterruptedException {
        HelloClient client = new HelloClient("127.0.0.1", 50051);
        StreamObserver<HelloProto.HelloRequest> helloRequestStreamObserver = client.initHelloStreamClient();
        int count = 0;
        while (true) {
            Thread.sleep(1000);
            //服务调用
//            client.sayHelloStream("Java client" + ++count, helloRequestStreamObserver);

            // 简单RPC调用示例
             String content = client.sayHello("Java client" + ++count);
             System.out.println(content);

            if (count > 10) {
                return;
            }
        }
    }

}