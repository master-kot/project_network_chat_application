package network;

import java.net.Socket;

/**
 * Интерфейс Socket Thread Listener имеет следующие события:
 */
public interface SocketThreadListener {
    void onSocketThreadStart(SocketThread thread, Socket socket); //Socket Thread стартовал
    void onSocketThreadReady(SocketThread thread, Socket socket); //Socket Thread готов
    void onSocketThreadStop(SocketThread thread); //Socket Thread остановился
    void onReceiveString(SocketThread thread, Socket socket, String msg); //Принята строковое сообщение потоком
    void onSocketThreadException(SocketThread thread, Throwable throwable); //Возникло исключение на потоке
}