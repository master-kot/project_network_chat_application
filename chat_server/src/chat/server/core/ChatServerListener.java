package chat.server.core;

public interface ChatServerListener {
    void onServerMessage(String msg);
}