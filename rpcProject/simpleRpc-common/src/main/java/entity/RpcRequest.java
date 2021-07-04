package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RpcRequest {
    private String requestId;
    private String className;
    private String version;
    private String methodName;// 方法参数、方法返回值、
    private Object[] parameters;
    private Class<?>[] ParameterTypes;

}
