package entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RpcResponse {
    private String requestId;
    private Object result;
    private String error;
}
