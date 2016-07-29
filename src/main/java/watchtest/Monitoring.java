package watchtest;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

import static watchtest.Constants.WORKER_PATH;

public class Monitoring implements Watcher{
    ZooKeeper zk;
    String hostPort;

    public Monitoring(String hostPort) {
        this.hostPort = hostPort;
    }

    void startZK() throws IOException {
        zk = new ZooKeeper(hostPort, Constants.ZOOKEEPER_TIME_OUT, this);
    }

    private void stopZK() throws InterruptedException {
        zk.close();
    }

    @Override
    public void process(WatchedEvent event){
        switch(event.getState()) {
            case SyncConnected:
                try {
                    // Watcher 이벤트는 1회성이기 때문에 다시 watch를 걸어줘야 함.
                    switch(event.getType()) {
                        case None:
                            break;
                        case NodeChildrenChanged:
                        case NodeDeleted:
                        case NodeCreated:
                        case NodeDataChanged:
                            System.out.println(event.getPath() + "의 " + event.getType() + " 이벤트 발생.");
                            watch();
                            break;
                        default:
                            System.out.println("유효하지 않은 Watch Event 타입입니다.");
                            break;
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case Disconnected:
            case AuthFailed:
            case ConnectedReadOnly:
            case SaslAuthenticated:
            case Expired:
                System.out.println("하위 노드 변경 외의 State는 처리하지 않습니다. (" + event.getState() + ")");
                break;
            default:
                System.out.println("유효하지 않은 Watcher Event 상태값 입니다.");
        }
    }

    void watch() throws KeeperException, InterruptedException{
        for(String child : zk.getChildren(WORKER_PATH, this)){   // /workers znode에 watcher를 등록하여 하위 노드에 대한 변경 감지.
            Stat stat = new Stat();
            zk.getData("/workers/" + child, this, stat);        // /workers znode의 하위 노드에 whatcher를 등록하여 상태 변화 감지.
            System.out.println(child + " - " + stat);
        }
    }


    void init() throws KeeperException, InterruptedException {
        if( zk.exists(WORKER_PATH, false) == null) {
            zk.create(WORKER_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name) {
                    switch(KeeperException.Code.get(rc)) {
                        case CONNECTIONLOSS:
                            break;
                        case OK:
                            System.out.println(path + "를 생성하였습니다.");
                            try {
                                watch();
                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case NODEEXISTS:
                            System.out.println(path + "가 이미 존재합니다.");
                            break;
                        default:
                            System.out.println("유효하지 않은 결과 입니다. (rc : " + rc + ")");
                            break;

                    }
                }
            }, null);
        } else {
            watch();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        Monitoring m = new Monitoring(Constants.HOST);
        m.startZK();
        m.init();

        Thread.sleep(Constants.MONITORING_APP_TIME_OUT);

        m.stopZK();
    }
}
