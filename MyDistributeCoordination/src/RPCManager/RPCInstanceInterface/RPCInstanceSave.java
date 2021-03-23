package RPCManager.RPCInstanceInterface;

import java.util.ArrayList;

public class RPCInstanceSave {
    private ArrayList<RPCSelectInstance> list = new ArrayList<>();

    public RPCInstanceSave(){}
    public ArrayList<RPCSelectInstance> getList() {
        return list;
    }

    public void setList(ArrayList<RPCSelectInstance> list) {
        this.list = list;
    }
}
