package chat.server.core;

/**
 * Интерфейс для обработчика - слушателя сообщений,
 * возникающих в ChatServer
 */
public interface ChatServerListener {
    void onServerMessage(String msg);
}