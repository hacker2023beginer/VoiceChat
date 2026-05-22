package org.vladproj.client.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.UserAction;
import org.vladproj.exception.TcpClientException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientTcp {
    private static final Logger log = LogManager.getLogger("org.vladproj.client.connection.ClientTcp");
    private static final String PARSER_DELIMITER = " ";
    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "Error";
    private final InetAddress serverIp;
    private final int serverPort;
    private final int udpClientPort;
    private final String username;
    private final Socket socket;

    public ClientTcp(InetAddress serverIp, int serverPort, String username, int udpClientPort) {
        log.info("TcpClient is started");
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.username = username;
        this.udpClientPort = udpClientPort;
        try {
            this.socket = new Socket(this.serverIp, this.serverPort);
        } catch (IOException e) {
            log.fatal("Cannot start client on server_ip {} with server_port {}", this.serverIp, this.serverPort);
            throw new TcpClientException(e);
        }
        log.info("Client starts on server_ip {} with server_port {}", serverIp, serverPort);
    }

    public boolean register() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            StringBuilder request = new StringBuilder();
            request.append(UserAction.REGISTER.name());
            request.append(PARSER_DELIMITER);
            request.append(username);
            request.append(PARSER_DELIMITER);
            request.append(udpClientPort);
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

    public List<String> searchUsers(String prefix) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            String query = prefix == null || prefix.isBlank() ? "*" : prefix.trim();
            writer.println(UserAction.SEARCH_USERS.name() + PARSER_DELIMITER + username + PARSER_DELIMITER + query);
            String response = reader.readLine();
            if (response == null || response.isBlank() || response.startsWith(STATUS_ERROR) || !response.startsWith(STATUS_OK)) {
                return List.of();
            }
            String users = response.substring(STATUS_OK.length()).trim();
            if (users.isBlank()) {
                return List.of();
            }
            return Arrays.asList(users.split(","));
        } catch (IOException e) {
            log.warn("Cannot search users", e);
            return List.of();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("Cannot close tcp socket", e);
            }
        }
    }
}
