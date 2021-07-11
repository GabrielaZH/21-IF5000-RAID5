package com.example.apiclient.controller;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class Send {

    private final int PORT = 16789;
    private String HOST = "192.168.1.5";//TODO cambiar a que identifique el host donde esta corriendo
    DatagramSocket socket = null;


    public void sendFile(String routeFile){

        try {
            socket = new DatagramSocket();
        }
        catch(SocketException se){}

        Thread r = new Thread(new messageReceiver(socket));
        Thread s = new Thread(new FileSender(socket,HOST));
        r.start();
        s.start();

            try {


                FileInputStream source = new FileInputStream(routeFile);
                byte buf[]=new byte[1024];
                int i=0;
                while(source.available()!=0)
                {
                    buf[i]=(byte)source.read();
                    i++;
                }
                source.close();
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


    public void getFile(String fileName){

        try {
            socket = new DatagramSocket();
        }
        catch(SocketException se){}

        Thread r = new Thread(new FileReceiver(socket));
        Thread s = new Thread(new FileSender(socket,HOST));
        r.start();
        s.start();

        try {
            String request= "receive"+fileName;
            byte bufRequest[]=request.getBytes();
            InetAddress addressRequest = InetAddress.getByName(HOST);
            DatagramPacket packetRequest = new DatagramPacket(bufRequest, bufRequest.length, addressRequest, PORT);
            socket.send(packetRequest);

        } catch(IOException ioe){
            System.out.println("Server Offline...");
            JOptionPane.showMessageDialog(null, "Server Offline...");
            System.exit(0);
        } catch(NullPointerException npe){
        }

    }



    public class FileSender implements Runnable {

        public DatagramSocket sock;
        public String hostname = "localhost";

        public FileSender(DatagramSocket s, String h) {
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
//               sendMessage("Client Connected...");
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

                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }
    }


    class messageReceiver implements Runnable {
        DatagramSocket sock;
        byte buf[];

        messageReceiver(DatagramSocket s) {
            sock = s;
            buf = new byte[1024];
        }

        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);
                    String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println(received);
                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }//end of run
    }//end of ClassMessageReceiver

    class FileReceiver implements Runnable {
        DatagramSocket sock;
        byte buf[];

        FileReceiver(DatagramSocket s) {
            sock = s;
            buf = new byte[1024];
        }

        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);
                    String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    String receivedMsg = new String(buf, buf.length);
                    FileWriter fw = new FileWriter(new File("text1RecibidoCliente.txt"));
                    fw.write(receivedMsg.trim());
                    fw.flush();
                    fw.close();

//                    FileInputStream fileInputStream= new FileInputStream("text1RecibidoCliente.txt");
//                    fileInputStream.read(buf);
//                    fileInputStream.close();

                    System.out.println(received);
                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }//end of run
    }//end of ClassFileReceiver



}//end class


