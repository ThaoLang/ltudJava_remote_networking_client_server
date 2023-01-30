package server;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import static server.Server.*;

/**
 * server
 * Created by Lang Thao Thao
 * Date 12/21/2022 : 6:46 PM
 * Description
 */
public class ServerGUI extends JFrame{
    private JPanel panel;
    private JButton chooseAFolderButton;
    private JButton disconnect;
    private  JTextPane actionLogTextPane;
    private JLabel connectingClientsLabel;
    private JScrollPane ClientScrollPane;
    private JTable listClientTable;
    private JTextField pathTextField;
    private JButton trackButton;
    private JScrollPane logScrollPane;

    private Server server;
    private ArrayList<String> logs;
    private String currentFolder;
    private String curIP;
    private int curPort;

    private String path;

    public ServerGUI(){
        logs=new ArrayList<String>();
        currentFolder="";
        path="";

        listClientTable.setModel(new DefaultTableModel(null, new String[]{"IP","Port"}));
        listClientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = listClientTable.getSelectionModel();
        actionLogTextPane.setEditable(false);
        curIP="";
        curPort=-1;

        new Thread(()->{
            while(true){
                int row=listClientTable.getSelectedRow();
                if (row==-1){
                    actionLogTextPane.setText("");
                }
                else{
                    String ip=listClientTable.getValueAt(row,0).toString();
                    int port= Integer.parseInt(listClientTable.getValueAt(row,1).toString());
                    for (int i=0;i<clientList.size();i++){
                        if (clientList.get(i).getIP().equals(ip) && clientList.get(i).getPort()==port){
                            actionLogTextPane.setText(clientList.get(i).getLogs());
                        }
                    }

               }
            }
        }).start();



        chooseAFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                path="";
                JFrame frame = new JFrame("Choose a folder");
//                frame.setSize(500,500);
//                DefaultMutableTreeNode root=new DefaultMutableTreeNode("");
//                DefaultMutableTreeNode D=new DefaultMutableTreeNode("D:\\");
//                DefaultMutableTreeNode C=new DefaultMutableTreeNode("C:\\");
//                DefaultMutableTreeNode E=new DefaultMutableTreeNode("E:\\");
//                C.add(E);
//                root.add(C);
//                root.add(D);
//                JTree tree=new JTree(root);
//                frame.add(tree);
//                frame.setVisible(true);
//                tree.addTreeSelectionListener(new TreeSelectionListener() {
//
//                    @Override
//                    public void valueChanged(TreeSelectionEvent e) {
//                        DefaultMutableTreeNode selectedNode =
//                                (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
//
//                        path=path+selectedNode;
//                        System.out.println(path);
//                        pathTextField.setText(path);
//
//                        int row=listClientTable.getSelectedRow();
//                        String ip=listClientTable.getValueAt(row,0).toString();
//                        int port= Integer.parseInt(listClientTable.getValueAt(row,1).toString());
//                        for (int i=0;i<clientList.size();i++){
//                            if (clientList.get(i).getIP().equals(ip) && clientList.get(i).getPort()==port){
//                                clientList.get(i).sendSubfolderRequest(path);
//                            }
//                        }
//                    }
//                });
//                tree.addMouseListener(new MouseAdapter() {
//                    public void mouseClicked(MouseEvent me) {
//
//                        TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
//                        if (tp != null) {
//                            String p=transferIntoPath(tp.getPath());
//
//                            //String lastNode=tp.getLastPathComponent().toString();
//                            pathTextField.setText(transferIntoPath(tp.getPath()));
//                        }
//
//                        else
//                            pathTextField.setText("");
//                    }
//                });
//            }});
//
////            void doMouseClicked(MouseEvent me) {
////
////            }


                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    String path=fileChooser.getCurrentDirectory().getAbsolutePath();
                    pathTextField.setText(file.getAbsolutePath());
                } else {
                    actionLogTextPane.setText("Open command canceled");
                }
            }
        });

        server=new Server(listClientTable,ClientScrollPane,actionLogTextPane,curIP,curPort);

        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row=listClientTable.getSelectedRow();
                if (row==-1){
                    JOptionPane.showMessageDialog(null,"Please choose a client");
                }
                else{
                    String ip=listClientTable.getValueAt(row,0).toString();
                    int port= Integer.parseInt(listClientTable.getValueAt(row,1).toString());
                    for (int i=0;i<clientList.size();i++){
                        if (clientList.get(i).getIP().equals(ip) && clientList.get(i).getPort()==port){
                            clientList.get(i).disconnect();
                            clientList.remove(i);
                            updateTable();
                        }
                    }

                    JOptionPane.showMessageDialog(null,"Disconnect successfully");
                }

            }
        });

        trackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listClientTable.getSelectionModel().isSelectionEmpty()){
                    JOptionPane.showMessageDialog(null,"Please choose a client");
                }
                else{
                    if (pathTextField.getText().isEmpty()){
                        JOptionPane.showMessageDialog(null,"Please choose a folder");
                    }

                    else{
                        actionLogTextPane.setText("");
                        String path=pathTextField.getText();
                        int row=listClientTable.getSelectedRow();
                        String ip=listClientTable.getValueAt(row,0).toString();
                        int port= Integer.parseInt(listClientTable.getValueAt(row,1).toString());

//                        //stop to track from last client
//                        for (int i=0;i<clientList.size();i++){
//                            if (clientList.get(i).getIP().equals(lastIP) && clientList.get(i).getPort()==lastPort){
//                                clientList.get(i).stopTrack();
//                            }
//                        }
//                        lastIP=ip;
//                        lastPort=port;

                        for (int i=0;i<clientList.size();i++){
                            if (clientList.get(i).getIP().equals(ip) && clientList.get(i).getPort()==port){
                                clientList.get(i).sendTrackFolder(path);
                            }
                        }
                        System.out.println(path);
                    }
                }

            }
        });
        listClientTable.addContainerListener(new ContainerAdapter() {
        });
        listClientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row=listClientTable.getSelectedRow();
                String ip=listClientTable.getValueAt(row,0).toString();
                int port= Integer.parseInt(listClientTable.getValueAt(row,1).toString());
                curIP=ip;
                curPort=port;
                for (int i=0;i<clientList.size();i++){
                    if (clientList.get(i).getIP().equals(ip) && clientList.get(i).getPort()==port){
                        pathTextField.setText(clientList.get(i).getCurrentTrackingPath());

                        actionLogTextPane.setText(clientList.get(i).getLogs());
                    }
                }

            }
        });

    }

    public String transferIntoPath(Object[] p){
        String path="";
        for (int i=1;i<p.length;i++){
            path=path+p[i].toString();
        }
        return path;
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Server");
        frame.setContentPane(new ServerGUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
