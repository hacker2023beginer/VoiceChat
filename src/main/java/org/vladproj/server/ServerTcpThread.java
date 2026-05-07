package org.vladproj.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vladproj.entity.ClientInfo;
import org.vladproj.entity.UserAction;
import org.vladproj.exception.ServerTcpException;
import org.vladproj.parser.ClientBufferParser;

import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerTcpThread extends Server {
    private static final Logger log = LogManager.getLogger();
    private static final ClientBufferParser parser = new ClientBufferParser();
    private static final int THREAD_NUM = 5;
    private static final int TCP_SOCKET_PORT = 5000;
    private ExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;


    public ServerTcpThread(String name) {
        super(name);
        try {
            this.serverSocket = new ServerSocket(TCP_SOCKET_PORT);
        } catch (IOException e) {
            log.fatal("Cannot start client with server_port {}", TCP_SOCKET_PORT);
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

    public void run() {
        log.info("Сервер TCP запущен и ожидает подключений...");

        try {
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (SocketException e) {
            if (!isRunning) {
                log.info("Сервер успешно остановлен.");
            } else {
                log.error("Ошибка сокета: ", e);
            }
        } catch (IOException e) {
            log.error("Ошибка ввода-вывода: ", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(20, TimeUnit.SECONDS)){
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.fatal("Программа завершена извне", e);
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
                log.error("Получено пустое сообщение от {}", clientSocket.getInetAddress());
                writer.println("Error empty message");
                return;
            }

            Optional<String[]> optionalData = parser.parseClient(line);
            if (optionalData.isEmpty() || optionalData.get().length < 3) {
                log.error("Некорректный формат данных: {}", line);
                writer.println("Error incorrect packet data");
                return;
            }

            //data[0] - method type; data[1] - username; data[2] - port
            String[] data = optionalData.get();

            UserAction.find(data[0]).ifPresentOrElse(
                    action -> {
                        ClientInfo client = new ClientInfo(clientSocket.getInetAddress(), Integer.parseInt(data[2]));
                        try {
                            process(action, data[1], client);
                        } catch (IllegalArgumentException e) {
                            writer.println("Error illegal function");
                        }
                    },
                    () -> {
                        log.error("Неизвестное действие: {}", data[0]);
                        writer.println("Error incorrect packet type");
                    }
            );
            writer.println("OK operation complete successful");
        } catch (IOException e) {
            log.error("Ошибка при работе с клиентом: ", e);
            e.printStackTrace();
        }
    }

    public boolean process(UserAction action, String username, ClientInfo client) {
        switch (action) {
            case REGISTER -> {
                if (!doLogin(username, client)){
                    log.error("Login method doesn't complete correct");
                    return false;
                }
            }
            case LOGOUT   -> {
                if (!doLogout(username)){
                    log.error("Logout method doesn't complete correct");
                    return false;
                }
            }
            default       -> throw new IllegalArgumentException("Unexpected value: " + action);
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
            log.error("Thread {} is crushed", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}
