import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZKDelete {
    private static ZooKeeper zk;
    private static ZooKeeperConnection conn;

    public static void delete(String path) throws KeeperException, InterruptedException {
        zk.delete(path, zk.exists(path, true).getVersion());
    }

    public static void main(String[] args) {
        String path = "/MyFirstZnode/mysecondsubnode";

        try {
            conn = new ZooKeeperConnection();
            zk = conn.connect("localhost");
            delete(path);
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
