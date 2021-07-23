
import com.rpc.server.rpcServer;
import com.rpc.server.service.helloWorldService;
import com.rpc.server.service.impl.helloWordServiceImpl;

/**
 * @author huangjiaxin
 */
public class serverMain {
    // 注册服务
    public static void main(String[] args) {
        String remoteAddress = "39.102.37.124:2181";
        String localAddress = "127.0.0.1:18877";
        rpcServer server = new rpcServer(remoteAddress,localAddress);
        helloWorldService helloword = new helloWordServiceImpl();
        server.addService(helloWorldService.class.getName(),"2.0",helloword);
        server.start();
    }
}
