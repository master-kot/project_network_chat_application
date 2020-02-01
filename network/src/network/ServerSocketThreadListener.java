package network;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Интерфейс слушателя сообщений на потоке Server Socket следующие методы (события):
 */
public interface ServerSocketThreadListener {
    void onServerSocketThreadStart(ServerSocketThread thread);      //Server Socket поток запущен
    void onServerSocketThreadStop(ServerSocketThread thread);       //Server Socket поток остановлен
    void onServerSocketCreate(ServerSocketThread thread, ServerSocket server); //Server Socket поток создан
    void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server); //Превышен таймаут в методе accept
    void onSocketAccept(ServerSocketThread thread, Socket socket);  //Принят сокет
    void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable); //Возникло исключение на потоке Server Socket
}