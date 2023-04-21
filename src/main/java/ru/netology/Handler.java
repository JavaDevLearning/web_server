package ru.netology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Handler extends Thread {

    private static final Map<String, String> CONTENTS_TYPES = new HashMap<>() {{
        put("png", "image/png");
        put("svg", "image/svg");
        put("html", "text/html");
        put("txt", "text/plain");
        put("css", "application/css");
        put("", "text/plain");
        put("js", "application/js");
    }};

    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";
    private Socket socket;
    private String directory;


    public Handler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    public Handler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            var url = this.getRequestURL(input);
            var filePath = Path.of(this.directory, url);

            if (url.contains("classic.html")) {
                var fileClassic = Files.readString(filePath);
                var content = fileClassic.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                var type = CONTENTS_TYPES.get("html");
                var fileBytes = content;
                this.sendHeader(output, 200, "OK", type, fileBytes.length);
                output.write(fileBytes);

            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                var extension = this.getFileExtension(filePath);
                var type = CONTENTS_TYPES.get(extension);
                var fileBytes = Files.readAllBytes(filePath);
                this.sendHeader(output, 200, "OK", type, fileBytes.length);
                output.write(fileBytes);
            } else {
                var type = CONTENTS_TYPES.get("text");
                this.sendHeader(output, 404, "Not Found", type, NOT_FOUND_MESSAGE.length());
                output.write(NOT_FOUND_MESSAGE.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getRequestURL(InputStream input) {
        var reader = new Scanner(input).useDelimiter("\r\n");
        var line = reader.next();
        return line.split(" ")[1];
    }

    private String getFileExtension(Path path) {
        var name = path.getFileName().toString();
        var extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long length) {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", length);

    }

}
