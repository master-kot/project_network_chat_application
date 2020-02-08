package chat.client;

import chat.library.Library;
import network.SocketThread;
import network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Графический интерфейс клиент чата на основе JFrame
 * Является слушателем сообщений SocketThreadListener
 */
public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    private static final int WIDTH = 400;   //ширина окна
    private static final int HEIGHT = 300;  //высота окна чата
    private static final String WINDOW_TITLE = "Chat Client";

    /**
     * Добавляем поля и кнопки
     */
    private final JTextArea log = new JTextArea();//основная область чата
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");//адрес сервера
    private final JTextField tfPort = new JTextField("8189");//порт сервера
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);
    private final JTextField tfLogin = new JTextField("Enter login");
    private final JPasswordField tfPassword = new JPasswordField("Enter password");
    private final JButton btnLogin = new JButton("Login");

    /**
     * Размещаем элементы на окне
     */
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();//поле ввода сообщения
    private final JButton btnSend = new JButton("Send");

    private final JList<String> userList = new JList<>();   //список пользователей чата
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    //пустой список пользователей показываемый пока клиент не авторизовался
    private final String[] EMPTY_LIST = new String[0];

    /**
     * Создаем новый экземпляр графической оболочки чат клиента
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    /**
     * Создаем новый экземпляр графической оболочки чат клиента
     */
    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);//расположение окна чата
        setSize(WIDTH, HEIGHT);     //размер окна чата
        setTitle(WINDOW_TITLE);     //название окна
        setAlwaysOnTop(true);       //поверх других окон

        log.setEditable(false);     //разрешить изменение поля лога чата
        log.setLineWrap(true);      //разрешить перенос текста
        JScrollPane scrollLog = new JScrollPane(log);       //панель лога
        JScrollPane scrollUsers = new JScrollPane(userList);//панель пользователей
        scrollUsers.setPreferredSize(new Dimension(100, 0));

        /*
         * Добавляем слушателей для кнопок и полей
         * все события обрабатываюстя здесь же в методе actionPerformed
         */
        cbAlwaysOnTop.addActionListener(this);
        btnLogin.addActionListener(this);
        btnSend.addActionListener(this);
        btnDisconnect.addActionListener(this);
        tfMessage.addActionListener(this);
        panelBottom.setVisible(false);
        panelTop.setVisible(true);

        /*
         * Добавляем компоненты графической части клиента
         */
        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        /*
         * Добавляем панели составных частей окна приложения
         */
        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
        add(scrollUsers, BorderLayout.EAST);
        setVisible(true);
    }

    /**
     * Обработчик неперехваченных исключений
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        showException(t, e);
        //выход из программы в случае возникновения неперехваченного исключения
        System.exit(1);
        //TODO
        //Лучше вылогиниваться и возвращаться к исходному состоянию ClientGUI
    }

    /**
     * Подключиться с созданием нового сокета и сокет треда
     */
    private void connect() {
        try {
            //создаем новый сокет
            Socket socket = new Socket(tfIPAddress.getText(),
                    Integer.parseInt(tfPort.getText()));
            //создаем новый сокет тред и передает туда сокет
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    /**
     * Обработчик событий
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());//поверх всех окон
        }  else if (src == btnSend || src == tfMessage) {
            sendMessage();          //отправить сообщение
        } else if (src == btnLogin || src == tfLogin ||
                src == tfPassword || src == tfPort ||
                src == tfIPAddress) {
            connect();              //подключиться к серверу по нажатию Enter
        } else if (src == btnDisconnect) {
            socketThread.close();   //отключиться от сервера
        } else {
            //TODO
            //бросать исключение в виде нового окна уведомлений
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    //TODO
    /* Реализовать приватные сообщения - возможность выбрать пользователя
     * и отправить сообщение только ему одному
     * Варианты выбора - нажать правой кнопкой на пользователя в списке
     * Либо
     */
    private void sendMessage() {
        String msg = tfMessage.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.requestFocusInWindow();
        socketThread.sendMessage(Library.getTypeBcastClient(msg));
    }

    //TODO
    /* Сделать логирование сообщений в базу данных
     * При этом можно создать отдельную базу для всех сообщений на сервере
     * и локальные базы для каждого клиента, сохраняющие его личную переписку
     */
    /**
     * Записываем сообщение в текстовый лог-файл
     */
    private void wrtMsgToLogFile(String msg) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    /**
     * Метод логирования сообщений
     */
    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
             @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
        //e.printStackTrace();
        StackTraceElement[] ste = e.getStackTrace();
        String msg = String.format("Exception in thread %s: %s: %s\n\t at %s",
                t.getName(),
                e.getClass().getCanonicalName(),
                e.getMessage(),
                ste[0]
        );
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        //TODO
        //логировать возникающие исключения
    }

    /**
     * Обработка событий SocketThread
     * */
    @Override
    public void onSocketThreadStart(SocketThread thread, Socket socket) {
        putLog("SocketThread start");
    }

    @Override
    public void onSocketThreadStop(SocketThread thread) {
        panelBottom.setVisible(false);      //нижняя панель невидима
        panelTop.setVisible(true);          //верхняя панель видима
        putLog("Connection lost...");
        setTitle(WINDOW_TITLE);
        userList.setListData(EMPTY_LIST);   //очистить список пользователей
    }

    @Override
    public void onSocketThreadReady(SocketThread thread, Socket socket) {
        putLog("Connection established");
        panelBottom.setVisible(true);       //нижняя панель видима
        panelTop.setVisible(false);         //верхняя панель невидима
        String login = tfLogin.getText();   //получить логин
        String password = new String(tfPassword.getPassword()); //получить пароль
        //посылаем аутентификационное сообщение после его обработки библиотекой
        thread.sendMessage(Library.getAuthRequest(login, password));
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
    }

    /**
     * Обработка исключения, возникшего на потоке, в этот метод
     * должны пробрасываться все исключения, возникшие в нижерасположенных
     * классах и затем показываться пользователю в виде всплывающих окон
     */
    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        //TODO
        //нужно логировать возникшее исключение
        showException(thread, throwable);
    }

    /**
     * Обработчик сообщений от клиента с использованием
     * методов преобразования сообщений из библиотеки Library
     */
    private void handleMessage(String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + ": " + arr[1]);
                break;
            case Library.AUTH_DENIED:
                putLog("Authentication denied, check your credentials!");
                socketThread.close();
                break;
            case Library.USER_LIST:
                String users = msg.substring(Library.USER_LIST.length() + Library.DELIMITER.length());
                String[] usersArr = users.split(Library.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Library.MSG_FORMAT_ERROR:
                //TODO
                //something here
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                //разбираем широковещательное сообщение
                putLog(dateFormat.format(Long.parseLong(arr[1])) + ": " + arr[2] + ": " + arr[3]);
                break;
            default:
                //TODO
                throw new RuntimeException("Unknown message type: " + msg);
                //логируем
        }
    }
}