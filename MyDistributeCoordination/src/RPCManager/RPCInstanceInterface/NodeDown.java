package RPCManager.RPCInstanceInterface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NodeDown extends Remote, Serializable {
    public static final long serialVersionUID = -1L;
    public void removeNodes(ArrayList<String> removeNodes) throws RemoteException;
}
