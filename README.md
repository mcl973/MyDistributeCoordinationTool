# MyDistributeCoordinationTool 分布式协调工具 

# 客户端连接非主协调节点的修改和获取分布式锁的操作选择
   节点的修改、删除、添加和分布式锁的获取中选择的是在协调节点内部实现暴露而不是将信息告诉给client，再由client来连接远程的主节点来实现节点数据的修改和分布式锁的获取。
主要的原因就是如果有一个节点有100个client去连接，那么选择第一种方式只需要在非协调主节点和协调主节点之间建立一个新的暴露即可，但是如果选择第二种方式会导致协调主节点多了100个新的连接。如果不是100个client呢，如10000个那么在协调主节点的原本的业务上会多了很多的业务处理。所以会影响协调主节点的性能。
所以本工具采用了暗度陈仓的方式来是的对于客户端来说是透明的方式。这样既可以实现数据的修改，也可以以高效的方式运行。 
# 更新了下分布式锁，去除了arrayList，使用了更加灵活和快速的自定义link来实现等待队列。
分布式锁的讲解在这里：
https://blog.csdn.net/qq_30761967/article/details/116233041
# 具体的radme在下面的连接：
https://blog.csdn.net/qq_30761967/article/details/115118244

# 参数配置
在Select.SelectConfig中配置的服务端的所有的节点的信息。
在RPCManager.RPC.RPCConfig中配置的是基于Select.SelectConfig的配置做了增改，即端口号在其基础上增加了100，并选出了master端口号，此配置主要是为了做主节点和副本之间的通信。
在NodeManager.Node.nodeConfig.nodeConfig中配置的是基于Select.SelectConfig的配置上本地端口号增加了200，此配置主要是为了向客户端服务的。


# 优化分布式锁以及优化增删改操作所带来的的数据同步
   ## 其中主节点的分布式锁结果展示：
10200
节点11101不是主节点
节点12102不是主节点
1618573215732<---->1618573215699
开始暴露
RMI TCP Connection(2)-192.168.127.1-----Thread-2
RMI TCP Connection(2)-192.168.127.1-----Thread-2
RMI TCP Connection(2)-192.168.127.1-----Thread-2获取锁
RMI TCP Connection(4)-192.168.127.1-----Thread-1
RMI TCP Connection(4)-192.168.127.1-----Thread-1加入队列等待
RMI TCP Connection(5)-192.168.127.1-----Thread-3
RMI TCP Connection(2)-192.168.127.1-----Thread-2锁释放
当前剩余的等待者数量为：0
RMI TCP Connection(4)-192.168.127.1-----Thread-1被唤醒
RMI TCP Connection(4)-192.168.127.1-----Thread-1
RMI TCP Connection(6)-192.168.127.1-----Thread-4
RMI TCP Connection(7)-192.168.127.1-----Thread-5
RMI TCP Connection(8)-192.168.127.1-----Thread-6
RMI TCP Connection(9)-192.168.127.1-----Thread-7
RMI TCP Connection(10)-192.168.127.1-----Thread-8
RMI TCP Connection(5)-192.168.127.1-----Thread-3
RMI TCP Connection(5)-192.168.127.1-----Thread-3获取锁
RMI TCP Connection(10)-192.168.127.1-----Thread-8加入队列等待
RMI TCP Connection(9)-192.168.127.1-----Thread-7加入队列等待
RMI TCP Connection(5)-192.168.127.1-----Thread-9
RMI TCP Connection(8)-192.168.127.1-----Thread-6加入队列等待
RMI TCP Connection(7)-192.168.127.1-----Thread-5加入队列等待
RMI TCP Connection(6)-192.168.127.1-----Thread-4加入队列等待
RMI TCP Connection(4)-192.168.127.1-----Thread-1加入队列等待
RMI TCP Connection(2)-192.168.127.1-----Thread-3锁释放
当前剩余的等待者数量为：6
RMI TCP Connection(11)-192.168.127.1-----Thread-10
RMI TCP Connection(5)-192.168.127.1-----Thread-9加入队列等待
RMI TCP Connection(11)-192.168.127.1-----Thread-10
RMI TCP Connection(5)-192.168.127.1-----Thread-9被唤醒
RMI TCP Connection(5)-192.168.127.1-----Thread-9
RMI TCP Connection(11)-192.168.127.1-----Thread-10获取锁
RMI TCP Connection(11)-192.168.127.1-----Thread-10锁释放
当前剩余的等待者数量为：6
RMI TCP Connection(10)-192.168.127.1-----Thread-8被唤醒
RMI TCP Connection(10)-192.168.127.1-----Thread-8
RMI TCP Connection(10)-192.168.127.1-----Thread-8
RMI TCP Connection(10)-192.168.127.1-----Thread-8获取锁
RMI TCP Connection(10)-192.168.127.1-----Thread-8锁释放
当前剩余的等待者数量为：5
RMI TCP Connection(9)-192.168.127.1-----Thread-7被唤醒
RMI TCP Connection(9)-192.168.127.1-----Thread-7
RMI TCP Connection(9)-192.168.127.1-----Thread-7
RMI TCP Connection(9)-192.168.127.1-----Thread-7获取锁
RMI TCP Connection(9)-192.168.127.1-----Thread-7锁释放
当前剩余的等待者数量为：4
RMI TCP Connection(8)-192.168.127.1-----Thread-6被唤醒
RMI TCP Connection(8)-192.168.127.1-----Thread-6
RMI TCP Connection(8)-192.168.127.1-----Thread-6
RMI TCP Connection(8)-192.168.127.1-----Thread-6获取锁
RMI TCP Connection(8)-192.168.127.1-----Thread-6锁释放
当前剩余的等待者数量为：3
RMI TCP Connection(7)-192.168.127.1-----Thread-5被唤醒
RMI TCP Connection(7)-192.168.127.1-----Thread-5
RMI TCP Connection(7)-192.168.127.1-----Thread-5
RMI TCP Connection(7)-192.168.127.1-----Thread-5获取锁
RMI TCP Connection(7)-192.168.127.1-----Thread-5锁释放
当前剩余的等待者数量为：2
RMI TCP Connection(6)-192.168.127.1-----Thread-4被唤醒
RMI TCP Connection(6)-192.168.127.1-----Thread-4
RMI TCP Connection(6)-192.168.127.1-----Thread-4
RMI TCP Connection(6)-192.168.127.1-----Thread-4获取锁
RMI TCP Connection(6)-192.168.127.1-----Thread-4锁释放
当前剩余的等待者数量为：1
RMI TCP Connection(4)-192.168.127.1-----Thread-1被唤醒
RMI TCP Connection(4)-192.168.127.1-----Thread-1
RMI TCP Connection(4)-192.168.127.1-----Thread-1
RMI TCP Connection(4)-192.168.127.1-----Thread-1获取锁
RMI TCP Connection(4)-192.168.127.1-----Thread-1锁释放
当前剩余的等待者数量为：0
RMI TCP Connection(5)-192.168.127.1-----Thread-9被唤醒
RMI TCP Connection(5)-192.168.127.1-----Thread-9
RMI TCP Connection(5)-192.168.127.1-----Thread-9
RMI TCP Connection(5)-192.168.127.1-----Thread-9获取锁
RMI TCP Connection(5)-192.168.127.1-----Thread-9锁释放
当前剩余的等待者数量为：0
   ## 其中分布式锁的客户端结果展示：
   监听的数据发生了改变。。。
1
Thread-1
Thread-2
Thread-3
/////////////上锁Thread-2
Thread-4
Thread-5
Thread-6
/////////////开锁Thread-2
成功--》1
Thread-7
/////////////上锁Thread-5
Thread-8
Thread-9
Thread-10
/////////////开锁Thread-5
成功--》2
/////////////上锁Thread-10
/////////////开锁Thread-10
成功--》3
/////////////上锁Thread-3
/////////////开锁Thread-3
成功--》4
/////////////上锁Thread-1
/////////////开锁Thread-1
成功--》5
/////////////上锁Thread-6
/////////////开锁Thread-6
成功--》6
/////////////上锁Thread-9
/////////////开锁Thread-9
成功--》7
/////////////上锁Thread-8
/////////////开锁Thread-8
成功--》8
/////////////上锁Thread-4
/////////////开锁Thread-4
成功--》9
/////////////上锁Thread-7
/////////////开锁Thread-7
成功--》10

# 增删改时的数据同步,目前只是简单的同步时局，没有实现分布式算法式的类似于zookeeper的过半机制同意策略
## 副本1
Select.SlaveRPCSelectInstance.synchronizedData->添加操作，操作的数据为：create
Select.SlaveRPCSelectInstance.synchronizedData->添加操作，操作的数据为：main
Select.SlaveRPCSelectInstance.synchronizedData->修改操作，操作的数据为：main
Select.SlaveRPCSelectInstance.synchronizedData->全局唯一id更新了，当前的id为：1
## 副本2
Select.SlaveRPCSelectInstance.synchronizedData->添加操作，操作的数据为：create
Select.SlaveRPCSelectInstance.synchronizedData->添加操作，操作的数据为：main
Select.SlaveRPCSelectInstance.synchronizedData->修改操作，操作的数据为：main
Select.SlaveRPCSelectInstance.synchronizedData->全局唯一id更新了，当前的id为：1
# 重新选主节点
## 副本1  （port为12002）
当前的主节点port是：11101
当前的主节点是：localhost:11001
Thread dead....
## 副本2 （port为11101）
当前的主节点port是：11101
当前的主节点是：localhost:11001
主节点已建立
Thread dead....
# 不支持锁的备份，如果在使用分布式锁时出现了主节点down掉需要重新在新的主节点上设置锁并拉去所对象

# 经测试当每一个线程之间间隔10毫秒时，分布式锁可以胜任10000个线程的抢夺。
   
