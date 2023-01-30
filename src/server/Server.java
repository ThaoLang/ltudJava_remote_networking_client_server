package server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

/**
 * server
 * Created by Lang Thao Thao
 * Date 12/21/2022 : 6:45 PM
 * Description
 */

public class Server {
    public static final int PORT=5000;
    private ServerSocket server;
    public static ArrayList<ClientHandler> clientList;

    private static JTable listClientTable;
    static JScrollPane ClientScrollPane;
    static JTextPane actionLogTextPane;
    static String curIP;
    static int curPort;

    Server(JTable table,JScrollPane scroll,JTextPane actionLog,String curIP,int curPort){
        clientList= new ArrayList<ClientHandler>();
        this.listClientTable=table;
        this.ClientScrollPane=scroll;
        this.actionLogTextPane=actionLog;
        this.curIP=curIP;
        this.curPort=curPort;
        try {
            server = new ServerSocket(PORT);
            server.setReuseAddress(true);

            new Thread(()->{
                try{
                    do{
                        Socket ss=server.accept();

                        ClientHandler client_thread=new ClientHandler(ss,this.actionLogTextPane,curIP,curPort);
                        clientList.add(client_thread);
                        updateTable();
                        new Thread (client_thread).start();
                    }while (server!=null && !server.isClosed());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTable(){
        if (clientList==null)
            return;

        Object[][] tableContent=new Object[clientList.size()][2];
        for (int i=0;i<clientList.size();i++){
            tableContent[i][0]=clientList.get(i).getIP();
            tableContent[i][1]=clientList.get(i).getPort();
        }

        String[] columnName={"IP","Port"};
        listClientTable.setModel(new DefaultTableModel(tableContent,columnName) {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        });
    }

    public class ClientHandler implements Runnable{
        private boolean exit;
        private Socket clientSocket;

        private  BufferedReader receiver;
        private BufferedWriter sender;

        private String logs;

        private String currentTrackingPath;
        static JTextPane actionLogTextPane;
        static String curIP;
        static  int curPort;

        public String getLogs() {
            return logs;
        }

        public void setLogs(String logs) {
            this.logs = logs;
        }

        public String getCurrentTrackingPath() {
            return currentTrackingPath;
        }

        public void setCurrentTrackingPath(String currentTrackingPath) {
            this.currentTrackingPath = currentTrackingPath;
        }
        public String getIP(){
            return clientSocket.getInetAddress().getHostAddress();
        }

        public int getPort(){
            return clientSocket.getPort();
        }
        public ClientHandler(Socket socket,JTextPane actionLog,String curIP,int curPort){
            this.curIP=curIP;
            this.curPort=curPort;
            this.clientSocket=socket;
            this.actionLogTextPane=actionLog;
            exit=false;
            currentTrackingPath="";
            logs="";
            try {
                InputStream is = clientSocket.getInputStream();
                receiver = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                OutputStream os  = clientSocket.getOutputStream();
                sender = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void run(){
            try {
                while(!exit) {
                    String message = receiver.readLine();
                    System.out.println("TTT"+message);

                    switch (message){
                        case "quit":{
                            removeFromList();
                            updateTable();
                            disconnect();
                            break;
                        }
                        case "subfolder list":{
                            new Thread(()->{

                            }).start();

                            break;
                        }

                        case "logs":{
                            String log;
//                            do{
//                                System.out.println("alo alo");
//                                log=receiver.readLine();
//                                System.out.println("coming: "+log);
//                                String text=actionLogTextPane.getText();
//                                System.out.println("TEXT"+text);
//                                //actionLogTextPane.getStyledDocument().insertString(actionLogTextPane.getStyledDocument().getLength(),log,null);
//                                actionLogTextPane.setText(text+"\n"+log);
//                                System.out.println("runnnn");
//
//                            }while(log.equals("stopped"));
                            System.out.println("alo alo");
                            log=receiver.readLine();
                            logs=logs+"\n"+log;
                            if (getIP()==curIP && getPort()==curPort)
                                actionLogTextPane.setText(log);
                            System.out.println("coming: "+log);
                            //String text=actionLogTextPane.getText();
                            //System.out.println("TEXT"+text);
                            //actionLogTextPane.getStyledDocument().insertString(actionLogTextPane.getStyledDocument().getLength(),log,null);
                            //actionLogTextPane.setText(text+"\n"+log);
                            System.out.println("runnnn");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                removeFromList();
                updateTable();
                disconnect();
            }
        }

        public void removeFromList(){
            for (int i = 0; i< clientList.size(); i++){
                if (clientList.get(i).getIP().equals(this.getIP())
                        && clientList.get(i).getPort()==this.getPort()){
                    clientList.remove(i);
                }
            }
        }

        public void disconnect(){
            try {
                sender.write("quit");
                sender.newLine();
                sender.flush();
            } catch (IOException e) {}

            exit=true;
            try {
                clientSocket.close();
            } catch (IOException e) {}
        }

        public void sendTrackFolder(String path){
            try {
                sender.write("track folder");
                sender.newLine();
                sender.flush();

                sender.write(path);
                sender.newLine();
                sender.flush();
                currentTrackingPath=path;
                logs="";

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

//        public void sendSubfolderRequest(String path){
//            try {
//                sender.write("send folder");
//                sender.newLine();
//                sender.flush();
//                sender.write(path);
//                sender.newLine();
//                sender.flush();
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
    }
}
