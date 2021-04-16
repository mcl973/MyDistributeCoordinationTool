package startMain;

public class test {
    public static void main(String[] args) throws ClassNotFoundException {
//        ClassLoader classLoader = Object.class.getClassLoader();
//        Class<?> aClass = classLoader.loadClass("F:\\mao\\java\\fourinone1" +
//                "\\MyDistributeCoordinationClient\\out\\production\\MyDistributeCoordinationClient\\Data\\SayHello.class");
        new test();
    }
    public test(){
        int[][] list = {{1,2},{3,4},{4,5},{5,6},{2,3},{6,4},{4,2},{8,9}};
        //阵营列表
        int temp[] = new int[list.length*2];
        for(int i=1;i<list.length+1;i++){
            //初始时，每一个人代表一个阵营
            temp[i] = i;
        }
        fen(list,temp);
        for (int i = 1;i<temp.length;i++) {
            System.out.println();
            System.out.println(temp[i]);
        }
    }

    public void fen(int[][] list,int[] temp){
        int a=0,b;
        for(int i=0;i<list.length;i++){
            b = temp[list[i][1]];
            if(b == list[i][1]){//如果b还没有被分配过
                temp[list[i][1]] = temp[list[i][0]];//开始分配
            }else{//将b所属的阵营转换到a来。
                a = temp[list[i][0]];
                if(a != b)
                    setint(temp,b,a);
            }
        }
    }
    //核心就在这里
    public void setint(int[] temp,int k,int value){
        for(int i=0;i<temp.length;i++){
            if(temp[i] == k)
                temp[i] = value;
        }
    }
}
