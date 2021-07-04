package utils;

/**
 * 常量
 *
 * @author 傲辰
 * @data 2021/6/30
 */
public interface Constant {
        int ZK_SESSION_TIMEOUT = 5000;
        int ZK_CONNECTION_TIMEOUT = 5000;

        String ZK_REGISTRY_PATH = "/registry";
        String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

        String ZK_NAMESPACE = "netty-rpc";

}
