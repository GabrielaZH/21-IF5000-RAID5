import huffman.HuffmanEncoding;
import java.io.*;
import java.net.*;
import java.util.Random;

public class Send {

    private final int PORT = 16789;
    private String HOST = "192.168.1.5";
    DatagramSocket socket = null;
    String fileNameReceive="";
    String pathFileSave="C:\\21-IF5000-RAID5\\Processos_Disk_Nodes_Java\\files\\";
    String pathFileReceive= "C:\\21-IF5000-RAID5\\Processos_Disk_Nodes_Java\\filesReceive\\";
    String pathFile="";
    HuffmanEncoding huffman = new HuffmanEncoding();//encode the file
    String pathFileDecode="";
    public void sendFile(File file) throws IOException {
        try {
            socket = new DatagramSocket();
            Thread r = new Thread(new messageReceiver(socket));
            Thread s = new Thread(new FileSender(socket,HOST));
            r.start();
            s.start();

            //send file name
            sendData(file.getName());

            //send data file
            File fileSaved= new File(pathFileSave+file.getName());
            File fileOut= new File(pathFileSave+"out.txt");
            huffman.encoding(fileSaved.getPath(),pathFileSave+"out.txt");
            File finalFile= new File(pathFileSave+"out.txt");
            sendPacket(finalFile);

            } catch(IOException ioe){
                throw ioe;
            } catch(NullPointerException npe){
                throw npe;
            }
    }



    public void getFile(String fileName) throws IOException {
       //fileNameReceive=fileName;

        try {
            socket = new DatagramSocket();
            Thread r = new Thread(new FileReceiver(socket,fileName));
            Thread s = new Thread(new FileSender(socket,HOST));
            r.start();
            s.start();

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
                        Thread.sleep(100);
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
        String finalName;
        FileReceiver(DatagramSocket s, String name) {
            sock = s;
            buf = new byte[60000];
            finalName=name;
        }


        public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    sock.receive(packet);

                    String pathFileEncode =pathFileReceive+"encode.txt";
                    File fileEncode = new File(pathFileEncode);

                    try (OutputStream os = new FileOutputStream(fileEncode)) {
                        os.write(buf);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
                    }
                    
                    pathFileDecode=pathFileReceive+finalName;
                    HuffmanEncoding huffman = new HuffmanEncoding();
                    huffman.decode(pathFileEncode,pathFileDecode);

                }
                catch(Exception e) {
                    System.err.println(e);
                }

       }//end of run
    }//end of ClassFileReceiver

//    public void saveFile(MultipartFile file, String path)  {
//        File fileToSave = new File(path+file.getName());
//        try (OutputStream os = new FileOutputStream(fileToSave)) {
//            InputStream initialStream = file.getInputStream();
//            byte[] buffer = new byte[initialStream.available()];
//            initialStream.read(buffer);
//            os.write(buffer);
//        } catch (Exception e) {
//            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
//        }
//    }

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
        byte buf[]=new byte[60000];
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


