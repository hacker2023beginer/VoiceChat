package org.vladproj.server.util;

public class ChatUtils {

    private ChatUtils(){}

    public static String buildChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0
                ? u1 + "_" + u2
                : u2 + "_" + u1;
    }
}
