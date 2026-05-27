package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.UserAction;
import org.vladproj.exception.ServerTcpException;
import org.vladproj.parser.ClientBufferParser;
import org.vladproj.server.util.FindUserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerTcpThread extends ServerRepository {
    private static final Logger log = LogManager.getLogger();
    private static final ClientBufferParser parser = new ClientBufferParser();
    private static final int THREAD_NUM = 5;
    public static final int TCP_SOCKET_PORT = 5000;
    private ExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;

    public ServerTcpThread(String name) {
        super(name);
        try {
            this.serverSocket = new ServerSocket(TCP_SOCKET_PORT);
        } catch (IOException e) {
            log.fatal("Cannot start server with tcp port {}", TCP_SOCKET_PORT);
            throw new ServerTcpException(e);
        }
        executor = Executors.newFixedThreadPool(THREAD_NUM);
        try {
            log.info("Server start with ip {} on port: {}", InetAddress.getLocalHost(), TCP_SOCKET_PORT);
        } catch (UnknownHostException e) {
            log.fatal("Cannot find ipv4 of server");
            Thread.currentThread().interrupt();
        }
    }

    public boolean doLogin(String username, ClientInfo clientInfo) {
        ClientInfo value = clients.putIfAbsent(username, clientInfo);
        if (value != null) {
            log.warn("There is user in hashmap with username: {}", username);
            return false;
        }
        log.info("Add user {} successful", username);
        return true;
    }

    public boolean doLogout(String username) {
        if (!clients.containsKey(username)) {
            log.warn("There is no user in hashmap with username: {}", username);
            return false;
        }
        clients.remove(username);
        log.info("Logout user {} successful", username);
        return true;
    }

    @Override
    public void run() {
        log.info("TCP server is started and waiting for clients...");
        try {
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (SocketException e) {
            if (isRunning) {
                log.error("Socket error: ", e);
            }
        } catch (IOException e) {
            log.error("IO error: ", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {

            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                writer.println("Error empty message");
                return;
            }

            Optional<String[]> optionalData = parser.parseClient(line);
            if (optionalData.isEmpty() || optionalData.get().length < 3) {
                writer.println("Error incorrect packet data");
                return;
            }

            String[] data = optionalData.get();
            Optional<UserAction> optionalAction = UserAction.find(data[0]);
            if (optionalAction.isEmpty()) {
                writer.println("Error incorrect packet type");
                return;
            }

            UserAction action = optionalAction.get();
            if (action == UserAction.SEARCH_USERS) {
                writer.println("OK " + String.join(",", findUsers(data[1], data[2])));
                return;
            }

            int udpPort;
            try {
                udpPort = Integer.parseInt(data[2]);
            } catch (NumberFormatException e) {
                writer.println("Error incorrect port");
                return;
            }
            ClientInfo client = new ClientInfo(clientSocket.getInetAddress(), udpPort, System.currentTimeMillis());
            if (!process(action, data[1], client)) {
                writer.println("Error user already exist");
                return;
            }
            writer.println("OK operation complete successful");
        } catch (IOException e) {
            log.error("Error while handling client", e);
        } catch (IllegalArgumentException e) {
            log.error("Incorrect client request", e);
        }
    }

    private List<String> findUsers(String currentUsername, String prefix) {
        String normalizedPrefix = "*".equals(prefix) ? "" : prefix;
        FindUserUtil findUserUtil = new FindUserUtil();
        clients.keySet().stream()
                .filter(username -> !username.equals(currentUsername))
                .forEach(findUserUtil::insert);
        List<String> foundUsers = new ArrayList<>(findUserUtil.searchPrefix(normalizedPrefix));
        Collections.sort(foundUsers);
        return foundUsers;
    }

    public boolean process(UserAction action, String username, ClientInfo client) {
        switch (action) {
            case REGISTER -> {
                if (!doLogin(username, client)) {
                    log.error("Login method doesn't complete correct");
                    return false;
                }
            }
            case LOGOUT -> {
                if (!doLogout(username)) {
                    log.error("Logout method doesn't complete correct");
                    return false;
                }
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + action);
        }
        return true;
    }

    @Override
    public void shutdown() {
        isRunning = false;
        try {
            serverSocket.close();
            log.info("Thread {} is interrupted", Thread.currentThread().getName());
        } catch (IOException e) {
            log.error("Thread {} is crashed", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}
