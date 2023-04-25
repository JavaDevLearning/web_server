package ru.netology;


import java.util.List;

public class Main {
    public static void main(String[] args) {

        var server = new Server(8080, "/Users/andreybelkin/Desktop/jspr-code-master/01_web/http-server/public", 4);
        server.upServer();
    }


}


