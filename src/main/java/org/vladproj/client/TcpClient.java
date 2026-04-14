package org.vladproj.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.UserAction;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
    private static final Logger log = LogManager.getLogger();
    private static final String PARSER_DELIMITER = " ";
    private final InetAddress SERVER_IP;
    private final int SERVER_PORT;
    private final int UDP_CLIENT_PORT;
    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "Error";
    private String username;
    private Socket socket;

    public TcpClient(InetAddress SERVER_IP, int SERVER_PORT, String username, int UDP_CLIENT_PORT) {
        log.info("TcpClient is started");
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
        this.username = username;
        this.UDP_CLIENT_PORT = UDP_CLIENT_PORT;
        log.info("Client starts on server_ip {} with server_port {}", SERVER_IP, SERVER_PORT);
    }

    public boolean register() {
        try {
            this.socket = new Socket(SERVER_IP, SERVER_PORT);
        } catch (IOException e) {
            log.fatal("Cannot start client on server_ip {} with server_port {}", SERVER_IP, SERVER_PORT);
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            StringBuilder request = new StringBuilder();
            request.append(UserAction.REGISTER.name());
            request.append(PARSER_DELIMITER);
            request.append(username);
            request.append(PARSER_DELIMITER);
            request.append(UDP_CLIENT_PORT);
            writer.println(request);
            String response = reader.readLine();
            if (response == null || response.isBlank()){
                log.info("Bad response from server");
                return false;
            }
            if (response.startsWith(STATUS_ERROR)) {
                log.info("Error status from server is received");
                return false;
            } else if (!response.startsWith(STATUS_OK)){
                log.info("Incorrect status from server is received");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("Status OK received from server");
        return true;
    }
}
