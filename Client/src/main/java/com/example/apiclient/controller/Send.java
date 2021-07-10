package com.example.apiclient.controller;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Send {

    private final int PORT = 16789;
    private String HOST = "192.168.1.5";//TODO cambiar a que identifique el host donde esta corriendo
    DatagramSocket socket = null;

    //TODO  actualmente esta que envia un mensaje y el server le devuelve ese mismo
    public void sendRequest(String senderMsg){

        try {
            socket = new DatagramSocket();
        }
        catch(SocketException se){}
//        HOST = JOptionPane.showInputDialog("Please enter the server's IP address: \nDefaults to localHost");
//
//        System.out.println("HOST = "+ HOST);
        Thread r = new Thread(new MessageReceiver(socket));
        Thread s = new Thread(new MessageSender(socket,HOST));
        r.start();
        s.start();

            try {
                byte buf[] = senderMsg.getBytes();
                InetAddress address = InetAddress.getByName(HOST);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
                socket.send(packet);
            } catch(IOException ioe){
                System.out.println("Server Offline...");
                JOptionPane.showMessageDialog(null, "Server Offline...");
                System.exit(0);
            } catch(NullPointerException npe){
            }

    }



    class MessageSender implements Runnable {

        public DatagramSocket sock;
        public String hostname = "localhost";

        public MessageSender(DatagramSocket s, String h) {
            sock = s;
            hostname = h;
        }

        public void sendMessage(String s){
            try{
                byte buf[] = s.getBytes();
                InetAddress address = InetAddress.getByName(hostname);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
                sock.send(packet);
            }
            catch(UnknownHostException uhe){
                System.out.println(uhe.getMessage());
            }
            catch(IOException ioe){
                System.out.println(ioe.getMessage());
            }
        }


        public void run() {
            boolean connected = false;
            do {
                try {
                    sendMessage("User Connected...");
                    connected = true;
                }
                catch (Exception e) {
                }
            } while (!connected);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    while (!in.ready()) {
                        Thread.sleep(100);
                    }
                    sendMessage(in.readLine());
                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }
    }


    class MessageReceiver implements Runnable {
        DatagramSocket sock;
        byte buf[];

        MessageReceiver(DatagramSocket s) {
            sock = s;
            buf = new byte[1024];
        }

        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);
                    String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println(received+"del server");
                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }//end of run
    }//end of ClassMessageReceiver

}//end class


