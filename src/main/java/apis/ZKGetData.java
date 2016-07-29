package apis;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class ZKGetData {
    private static ZooKeeper zk;
    private static ZooKeeperConnection conn;

    public static Stat znode_exists(String path) throws KeeperException, InterruptedException {
        return zk.exists(path, true);
    }

    public static void main(String[] args) {
        String path = "/MyFirstZnode";
        final CountDownLatch connectedSignal = new CountDownLatch(1);

        try {
            conn = new ZooKeeperConnection();
            zk = conn.connect("localhost");
            Stat stat = znode_exists(path);

            if (stat != null) {
                byte[] b = zk.getData(path, event -> {
                    if(event.getType() == Watcher.Event.EventType.None) {
                        switch(event.getState()) {
                            case Expired:
                                connectedSignal.countDown();
                                break;
                        }
                    } else {
                        String path1 = "/MyFirstZnode";

                        try{
                            byte[] bn = zk.getData(path1, false, null);
                            String data = new String(bn, "UTF-8");
                            System.out.println(data);
                            connectedSignal.countDown();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);

                String data = new String(b, "UTF-8");
                System.out.println(data);

                connectedSignal.await();
            } else {
                System.out.println("Node does not exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
