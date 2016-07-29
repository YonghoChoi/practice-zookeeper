package apis;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class ZKSetData {
    private static ZooKeeper zk;
    private static ZooKeeperConnection conn;

    public static void update(String path, byte[] data) throws KeeperException, InterruptedException {
        zk.setData(path, data, zk.exists(path, true).getVersion());
    }

    public static void main(String[] args) {
        String path = "/MyFirstZnode";
        byte[] data = "Success".getBytes();

        try {
            conn = new ZooKeeperConnection();
            zk = conn.connect("localhost");
            update(path, data);
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
