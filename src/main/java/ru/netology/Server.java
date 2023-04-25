package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int port;
    private String directory;
    private ExecutorService pool;


    public Server(int port, String directory, int poolSize) {
        this.port = port;
        this.directory = directory;
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void upServer() {
        try (var serverSocket = new ServerSocket(this.port)) {
            while (true) {
                var socket = serverSocket.accept();
                var handler = new Handler(socket, this.directory);
                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}
