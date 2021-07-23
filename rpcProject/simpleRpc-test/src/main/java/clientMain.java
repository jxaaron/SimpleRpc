import com.rpc.client.RpcClient;
import com.rpc.server.service.helloWorldService;

public class clientMain {
    public static void main(String[] args) throws Exception {
        String zkAddress="39.102.37.124:2181";
        RpcClient rpcClient=new RpcClient(zkAddress);
        helloWorldService helloWorld=RpcClient.createService(helloWorldService.class,"2.0");
        String result =helloWorld.sayHello("xiaozi");
        System.out.println(result);

        // 测试缓存文件删除
    }

}
