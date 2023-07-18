package com.autohealer;

import org.apache.zookeeper.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;

public class Autohealer implements Watcher {

    private static final Logger LOG = Logger.getLogger(Autohealer.class.getName());

    private final int NUMBER_OF_WORKERS;
    private final String PATH_TO_WORKER_JAR;

    private static final String AUTOHEALER_ZNODES_PATH = "/clinic_adm";

    private static final String ZK_ADDRESS = "localhost:2181";
    private static final int ZK_SESSION_TIMEOUT = 3000;

    private ZooKeeper zooKeeper;

    public Autohealer(int numberOfWorkers, String pathToWorkerProgram) {
        NUMBER_OF_WORKERS = numberOfWorkers;
        PATH_TO_WORKER_JAR = pathToWorkerProgram;
    }

    public void connectToZookeeper() throws IOException {
        zooKeeper = new ZooKeeper(ZK_ADDRESS, ZK_SESSION_TIMEOUT, this);
    }

    private void createWorkersIfNecessary() {

        try {
            List<String> children = zooKeeper.getChildren(AUTOHEALER_ZNODES_PATH, this);
            LOG.info(String.format("There are currently %s workers", children.size()));

            if (children.size() < NUMBER_OF_WORKERS) {
                launchNewWorker();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchNewWorker() throws IOException {

        LOG.info("Launching new worker...");
        File jar = new File(PATH_TO_WORKER_JAR);
        String cmd = "java -jar " + jar.getCanonicalPath();
        Runtime.getRuntime().exec(cmd, null, jar.getParentFile());
    }

    public void startMonitoringWorkers() throws InterruptedException, KeeperException {

        if (zooKeeper.exists(AUTOHEALER_ZNODES_PATH, false) == null) {
            zooKeeper.create(AUTOHEALER_ZNODES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        createWorkersIfNecessary();
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected)
                    LOG.info("Successfully connected to Zookeeper");
                else {
                    synchronized (zooKeeper) {
                        LOG.info("Disconnected from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeChildrenChanged:
                LOG.info(NodeChildrenChanged.toString());
                createWorkersIfNecessary();
        }
    }
}
