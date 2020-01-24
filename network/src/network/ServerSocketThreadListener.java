package network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {
    /**
     * Интерфейс слушателя потока Server Socket Thread имеет следующие события:
     */
    void onServerSocketThreadStart(ServerSocketThread thread); //Server Socket поток запущен
    void onServerSocketThreadStop(ServerSocketThread thread); //Server Socket поток остановлен
    void onServerSocketCreate(ServerSocketThread thread, ServerSocket server); //Server Socket поток создан
    void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server); //Таймаут
    void onSocketAccept(ServerSocketThread thread, Socket socket); //Принят сокет
    void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable); //Возникло исключение на потоке Server Socket
}