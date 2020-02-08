package chat.server.core;

import chat.library.Library;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

/**
 * Клиентский поток, создан чтобы обрабатывать
 * авторизацию пользователей согласно их credentials в базе данных
 */
public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuthorized;
    //если необходимо войти из другого экземпляра программы
    private boolean isReconnect;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isReconnect() {
        return isReconnect;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    void authAccept(String nickname) {
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage(Library.getAuthAccept(nickname));
    }

    void authFail() {
        sendMessage(Library.getAuthDenied());
        close();
    }

    void msgFormatError(String msg) {
        sendMessage(Library.getMsgFormatError(msg));
        close();
    }

    void reconnect() {
        isReconnect = true;
        close();
    }
}