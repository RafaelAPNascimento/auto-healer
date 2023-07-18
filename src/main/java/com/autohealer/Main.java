package com.autohealer;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        if (args.length != 2) {
            LOG.severe("Expecting parameters <number of workers> <path to worker jar file>");
            System.exit(1);
        }

        int numberOfWorkers = Integer.parseInt(args[0]);
        String pathToWorkerProgram = args[1];

        Autohealer autohealer = new Autohealer(numberOfWorkers, pathToWorkerProgram);
        autohealer.connectToZookeeper();
        autohealer.startMonitoringWorkers();
        autohealer.run();
        autohealer.close();
    }
}