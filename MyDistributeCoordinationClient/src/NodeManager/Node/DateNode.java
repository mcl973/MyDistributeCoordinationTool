package NodeManager.Node;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DateNode  implements Serializable, Comparable<DateNode>, Remote ,Cloneable{
    public static final long serialVersionUID = -1L;
    private byte[] values;
    public DateNode(){}
    public DateNode(byte[] values) throws RemoteException {
        this.values = values;
    }
    public byte[] getValues() {
        return values;
    }

    public void setValues(byte[] values) {
        this.values = values;
    }

    @Override
    public int compareTo(DateNode o) {
        if (this.getValues().length<o.getValues().length)
            return -1;
        else if(this.getValues().length>o.getValues().length)
            return 1;
        else
            return 0;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        DateNode dateNode = new DateNode();
        dateNode.setValues(this.getValues());
        return dateNode;
    }
}
