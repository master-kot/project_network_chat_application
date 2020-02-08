package chat.server.gui;

import chat.server.core.ChatServer;
import chat.server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Графическая оболочка сервера чата на основе фреймворка
 * Отвечает за запуск и остановку сервера чата
 * Класс явлеется обработчиком событий и возникающих исключений
 */
public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListener {
    /*
     * Задание расположения и размера окна графической оболочки
     */
    private static final int POS_X = 800;
    private static final int POS_Y = 200;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    /*
     * Создание экземпляра чат-сервера - бэкэнда чат сервера;
     */
    private final ChatServer chatServer = new ChatServer(this);
    /*
     * Создание необходимых кнопок графической оболочки
     */
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JPanel panelTop = new JPanel(new GridLayout(1, 2));
    private final JTextArea log = new JTextArea();  //лог сервера чата

    /**
     * Запуск графической оболочки с созданием экземпляра класса ServerGUI
     * в специализированном потоке SwingUtilities.invokeLater
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerGUI();
            }
        });
    }


    private ServerGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Thread.setDefaultUncaughtExceptionHandler(this);//Делаем экземпляр класса ServerGUI слушателем исключений
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle("Server GUI");
        setResizable(false);
        setAlwaysOnTop(true);
        log.setEditable(false); //возможность изменения размера
        log.setLineWrap(true);  //разрешить перенос строки
        JScrollPane scrollLog = new JScrollPane(log);   //лог панель
        //добавляем слушателей событий возникающих на графическом интерфейсе
        btnStop.addActionListener(this);
        btnStart.addActionListener(this);
        panelTop.add(btnStart);
        panelTop.add(btnStop);
        add(panelTop, BorderLayout.NORTH);  //панель с кнопками
        add(scrollLog, BorderLayout.CENTER);    //панель с логом
        setVisible(true);   //разрешить видимость оболочки
    }

    /**
     * События нажатия кнопок старт и стоп
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStart) {
            chatServer.start(8189);
        } else if (src == btnStop) {
            chatServer.stop();
        } else {
            /*
             * если источник события неизвестен - выбросить исключение
             * это заглушка, позже здесь будут другие кнопки,
             * а само исключение проброшено в Swing
             */
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void putLog(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //добавление логов в текстовом поле log
                log.append(msg + '\n');
                //переход на следующую строку
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    /**
     * Метод обработки возникших на графической части сервера исключений
     * @param t - поток, в котором возникло исключение e
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //TODO
       e.printStackTrace();
       StackTraceElement[] ste = e.getStackTrace();
       String msg = String.format("Exception in thread %s: %s: %s\n\t at %s",
               t.getName(),
               e.getClass().getCanonicalName(),
               e.getMessage(),
               ste[0]
       );
       //выброс окна с сообщениями о возникшей ошибке поверх окна приложения
       JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        putLog(msg);
    }

    @Override
    public void onServerMessage(String msg) {
        putLog(msg);
    }
}