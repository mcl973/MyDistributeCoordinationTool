package Data;

import java.io.Serializable;
import java.rmi.Remote;

/**
 * 用于测试使用
 */
public class SayHello implements Serializable {
    public static final long serialVersionUID = -1L;
    public String data = "hello";

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
