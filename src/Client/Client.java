package client;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.nio.file.StandardWatchEventKinds.*;


/**
 * Client
 * Created by Lang Thao Thao
 * Date 12/21/2022 : 6:47 PM
 * Description
 */
public class Client {
    private String ip;
    private int port;
    private boolean connect;

    private BufferedReader receiver;
    private BufferedWriter sender;

    private Socket s;

    private boolean stopTracking=false;

    static JButton connectButton;
    static JLabel IPLabel;
    static JLabel portLabel;
    static JOptionPane optionPane;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return String.valueOf(port);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

//    public String getError() {
//        return error;
//    }
//
//    public void setError(String error) {
//        this.error = error;
//    }

    public BufferedReader getReceiver() {
        return receiver;
    }

    public void setReceiver(BufferedReader receiver) {
        this.receiver = receiver;
    }

    public BufferedWriter getSender() {
        return sender;
    }

    public void setSender(BufferedWriter sender) {
        this.sender = sender;
    }

    public Socket getS() {
        return s;
    }

    public void setS(Socket s) {
        this.s = s;
    }

    public Client(JButton btn,JLabel ip, JLabel port,JOptionPane optionPane){
        this.connect=false;
        //error="";
        this.connectButton=btn;
        this.IPLabel=ip;
        this.portLabel=port;
        this.optionPane=optionPane;

    }

    public Client(String ip, int port, boolean connect) {
        this.ip = ip;
        this.port = port;
        this.connect = connect;
    }
    public String getDate(){
        Date date = new Date();
        SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return DateFor.format(date);
    }

    public void connectServer() throws Exception {
        //new Thread(()->{
            try
            {
                s = new Socket("localhost",5000);
                InputStream is = s.getInputStream();
                receiver = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                OutputStream os = s.getOutputStream();
                sender = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                setConnect(true);
                setIp(s.getLocalAddress().getHostAddress());
                setPort(s.getLocalPort());
            }
            catch(UnknownHostException ex)
            {
                //error="Server not found ";
                throw new Exception("Server not found ");
            } catch (IOException e) {
                //error="Server not found ";
                throw new Exception("Server not found ");
            }
        //}).start();

        new Thread(()->{
            while(!s.isClosed() && this.connect){
                try{
                    String message=receiver.readLine();
                    //if (message!=null){
                        switch (message){
                            case "quit":{
                                this.connectButton.setText("Connect");
                                this.IPLabel.setText("IP: disconnected");
                                this.portLabel.setText("Port: disconnected");
                                disconnect();
                            }
                            case "send folder":{
                                String path=receiver.readLine();
                                new Thread(()->{
                                    ArrayList<String>subfolders=getSubfolder(new File(path));
                                    try {
                                        for (int i=0;i<subfolders.size();i++){
                                            sender.write("subfolder list");
                                            sender.newLine();
                                            sender.flush();

                                            sender.write(subfolders.get(i));
                                            sender.newLine();
                                            sender.flush();
                                        }

                                        System.out.println(String.valueOf(subfolders));
                                    } catch (IOException e) {}
                                }).start();
                                break;
                            }
                            case "track folder":{
                                System.out.println("receive track folder");
                                String path=receiver.readLine();
//                                sender.write("logs");
//                                sender.newLine();
//                                sender.flush();
                                stopTracking=true;
                                new Thread(()->{monitorFolder(path);}).start();
                                break;
                            }

                        }


                }catch(IOException ioe){
                    //error="Server disconnected suddenly";
                    this.connectButton.setText("Connect");
                    this.IPLabel.setText("IP: disconnected");
                    this.portLabel.setText("Port: disconnected");
                    if (this.connect)
                        this.optionPane.showMessageDialog(null,"Server disconnected suddenly");
                    disconnect();
                }
            }
        }).start();


    }

    public void disconnect(){
        try {
            sender.write("quit");
            sender.newLine();
            sender.flush();

            //receiver.close();
            //sender.close();
            s.close();
            this.connect=false;
            this.ip="";
            this.port=-1;
            //this.error="";
        } catch (IOException e) {}
    }

    public ArrayList<String> getSubfolder(File dir) {
        ArrayList<String>subfolders=new ArrayList<String>();

        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    subfolders.add(listFile[i].getName().toString());
                }
            }
        }
        return subfolders;
    }
    public void monitorFolder(String p){
        System.out.println("tracking");
        Path path= Paths.get(p);
        stopTracking=false;

        try {
            WatchService watcherService= FileSystems.getDefault().newWatchService();
            WatchKey watchKey=path.register(watcherService,ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY);

            while(!stopTracking){
                watchKey=watcherService.take();
                for (WatchEvent<?> event: watchKey.pollEvents()){
                    WatchEvent<Path> watchEvent=(WatchEvent<Path>) (event);
                    Path fileName=watchEvent.context();
                    WatchEvent.Kind<?> kind=event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("hellooooo");
                        sender.write("logs");
                        sender.newLine();
                        sender.flush();

                        sender.write("["+this.getDate()+"]: "+ fileName+" is created  " );
                        sender.newLine();
                        sender.flush();
                        System.out.println("bye bye");
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        sender.write("logs");
                        sender.newLine();
                        sender.flush();

                        sender.write("["+this.getDate()+"]: "+ fileName+" has been deleted ");
                        sender.newLine();
                        sender.flush();
                        System.out.println("A file has been deleted: " + fileName);
                    }
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        sender.write("logs");
                        sender.newLine();
                        sender.flush();

                        sender.write("["+this.getDate()+"]: "+ fileName+" has been modified ");
                        sender.newLine();
                        sender.flush();
                        System.out.println("A file has been modified: " + fileName);
                    }
//                    System.out.println(event.kind().name().toString() + " "
//                            + path.resolve(watchEvent.context()));
                    boolean valid=watchKey.reset();
                    if (!valid ){
                        break;
                    }
                    if (stopTracking){
                        return;
                    }
                }
//                if (receiver.readLine().equals("stop tracking")){
//                    System.out.println("really");
//                    break;
//                }
                if(stopTracking)
                    return;
            }
            System.out.println("see you");
//            sender.write("stopped");
//            sender.newLine();
//            sender.flush();
        } catch (IOException e) {
            //throw new RuntimeException(e);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }

    }
}
