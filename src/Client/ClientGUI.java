package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Client
 * Created by Lang Thao Thao
 * Date 12/21/2022 : 6:47 PM
 * Description
 */
public class ClientGUI extends JFrame{
    private  JButton connectButton;
    private JPanel panel;
    private JLabel IPLabel;
    private JLabel portLabel;
    private JPanel infoClientPanel;

    private JOptionPane optionPane;
    private Client client;

    public ClientGUI(){
        client=new Client(connectButton,IPLabel,portLabel,optionPane);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!client.isConnect() && connectButton.getText()=="Connect"){
                    try{
                        client.connectServer();
                        connectButton.setText("Disconnect");
                        IPLabel.setText("IP: " + client.getIp());
                        portLabel.setText("Port: " + client.getPort());

                    }catch (Exception ex){
                        optionPane.showMessageDialog(null,"Server not found");
                    }
                }
                else{
                    disconnect();
                    //optionPane.showMessageDialog(null,"Disconnect successfully");
                }
            }
        });
    }
    public void disconnect(){
        client.disconnect();
        connectButton.setText("Connect");
        IPLabel.setText("IP: disconnected");
        portLabel.setText("Port: disconnected");
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("client");
        frame.setContentPane(new ClientGUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
