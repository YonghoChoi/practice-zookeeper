package watchtest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Worker2 {
    ZooKeeper zk;
    String hostPort;
    String path;

    public Worker2(String hostPort) {
        this.hostPort = hostPort;
        this.path = "Worker2";
    }

    void startZK() throws IOException {
        zk = new ZooKeeper(hostPort, Constants.ZOOKEEPER_TIME_OUT, null);
    }

    private void stopZK() throws InterruptedException {
        zk.close();
    }

    void createZnode() throws KeeperException, InterruptedException {
        zk.create(Constants.WORKER_PATH + "/" + path, path.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        Worker2 worker = new Worker2(Constants.HOST);
        worker.startZK();
        worker.createZnode();

        Thread.sleep(Constants.WORKER_APP_TIME_OUT);

        worker.stopZK();
    }
}
