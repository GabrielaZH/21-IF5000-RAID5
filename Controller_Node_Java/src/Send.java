import huffman.HuffmanEncoding;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;

public class Send {

    private final int PORT = 16787;
    private String HOST = "";
    DatagramSocket socket = null;
    String fileNameReceive="";
    String pathFileSave="C:\\21-IF5000-RAID5\\Controller_Node_Java\\files\\";
    String pathFileReceive= "C:\\21-IF5000-RAID5\\Controller_Node_Java\\filesReceive\\";
    String pathFile="";
    HuffmanEncoding huffman = new HuffmanEncoding();//encode the file
    String pathFileDecode="";


    public void sendFile(String name,String nodes) throws IOException {
        try {
            socket = new DatagramSocket();
            //send file name
            sendData("nodes:" + nodes);

            sendData(name);

        } catch(IOException ioe){
            throw ioe;
        } catch(NullPointerException npe){
            throw npe;
        }
    }

    public void sendFile(File file) throws IOException {
        try {
            socket = new DatagramSocket();

            //send file name
            sendData(file.getName());

            //send data file
            File fileOut= new File(pathFileSave+"out.txt");
            huffman.encoding(file.getPath(),pathFileSave+"out.txt");
            File finalFile= new File(pathFileSave+"out.txt");
            sendPacket(finalFile);

            } catch(IOException ioe){
                throw ioe;
            } catch(NullPointerException npe){
                throw npe;
            }
    }



    public void getFile(String fileName, int numReject, String numNodes) throws IOException {
        fileNameReceive=fileName;
        try {
            socket = new DatagramSocket();
            Thread r = new Thread(new FileReceiver(socket, numReject));
            Thread s = new Thread(new FileSender(socket,HOST));
            r.start();
            s.start();

            sendData("nodes:"+numNodes);
            //send file name
            sendData(fileName);
            //send type request
            sendData("receiveRequest");

        } catch(IOException ioe){
            throw ioe;
        } catch(NullPointerException npe){
            throw npe;
        }
    }



    public class FileSender implements Runnable {

        public DatagramSocket sock;
        public String hostname = "localhost";

        public FileSender(DatagramSocket s, String h) {
            sock = s;
            hostname = h;
        }

        public void run() {
            boolean connected = false;
            do {
                try {
                    connected = true;
                }
                catch (Exception e) {
                }
            } while (!connected);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    while (!in.ready()) {
                        Thread.sleep(90000000);
                    }

                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }
    }


   public class messageReceiver implements Runnable {
        DatagramSocket sock;
        byte buf[];
        messageReceiver(DatagramSocket s) {
            sock = s;
            buf = new byte[60000];
        }

        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);
                    String  received = new String(packet.getData(), packet.getOffset(), packet.getLength());
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
        int i;
        int numRejected;
        boolean flag;

        FileReceiver(DatagramSocket s, int numReject) {
            sock = s;
            buf = new byte[60000];
            numRejected = numReject;
        }

        public void run() {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);

                    String pathFileEncode = pathFileReceive + "encode.txt";
                    File fileEncode = new File(pathFileEncode);

                    try (OutputStream os = new FileOutputStream(fileEncode)) {
                        os.write(buf);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
                    }

                    if(i==numRejected){
                        i++;
                    }

                    if(!flag){
                        pathFileDecode = pathFileReceive +"file" +i+".txt";
                        HuffmanEncoding huffman = new HuffmanEncoding();
                        huffman.decode(pathFileEncode, pathFileDecode);
                    }else{
                        pathFileDecode = pathFileReceive + "fileParity" +i+".txt";
                        HuffmanEncoding huffman = new HuffmanEncoding();
                        huffman.decode(pathFileEncode, pathFileDecode);
                        i++;
                    }
                    flag=!flag;

                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        }//end of run
    }//end of ClassFileReceiver

    public void sendData(String fileName) throws IOException {
        writeInFile(fileName.getBytes(),pathFileSave+"tempNameFile.txt");
        File fileSaved= new File(pathFileSave+"tempNameFile.txt");
        File fileOut= new File(pathFileSave+"out.txt");
        huffman.encoding(fileSaved.getPath(),pathFileSave+"out.txt");
        File finalFile= new File(pathFileSave+"out.txt");
        sendPacket(finalFile);
    }

    public void sendPacket(File finalFile) throws IOException {
        FileInputStream source = new FileInputStream(finalFile);
        byte buf[]=new byte[65000];
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
    }
    public File writeInFile(byte[] text, String path)  {
        File fileToSave = new File(path);
        File finalFile;
        try (OutputStream os = new FileOutputStream(fileToSave)) {
            os.write(text);
            finalFile = new File(path);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
        return finalFile;
    }

}//end class


