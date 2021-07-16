import org.w3c.dom.ls.LSInput;

import java.io.*;
import java.util.*;

//TO DO
//* write disk0, disk1 and parity on real files (????)
//* check if the file was deleted to then recover it

public class Main {

    //region Attributes
    private static final String PATH = "C:\\RAID5\\";

    private static final List<String> DISK = new ArrayList();
    private static final List<String> FILES = new ArrayList();

    private static final String DISK0 = "DISK0";
    private static final String DISK1 = "DISK1";
    private static String DISKDamaged = "";
    private static String lastDisk = "";
    private static final String PARITY = "PARITY";
    private static final String FILE0 = "File0.txt";
    private static final String FILE1 = "File1.txt";
    private static final String FILEPARITY = "FileParity.txt";
    private static final String FILERECOVERED = "Arquivo Recuperado.txt";
    private static PrintWriter w;
    //endregion

    //Cria diretórios que representaram os disco 0, 1 e de paridade REFATORADO
    private static void createDisks(List<String> disks){
        File disk;
        for (String name : disks){
            disk = new File(PATH, name);
            if (!disk.exists()){
                if (!disk.mkdirs()) System.out.printf("%s não pode ser criado!\n", name);
                else System.out.printf("%s criado com sucesso!\n", name);
            }
        }
    }


    //Converte o arquivo em bytes REFATORADO
    private static byte[] parseFileToByte(File file){

        int fileLength = (int)file.length();
        byte[] bytesArray = new byte[fileLength];
        FileInputStream fileLikeStream;

        try {
            fileLikeStream = new FileInputStream(file);
            fileLikeStream.read(bytesArray, 0, fileLength);

        } catch (FileNotFoundException fnfex) {
            System.out.println("Arquivo não encontrado!");
        } catch (IOException ioex) {
            System.out.println("Erro inesperado!");
        }

        return bytesArray;
    }

    //Grava os arquivos de bytes nos discos REFATORADO
    private static void saveFileWithRAID5(byte[] bytesArray, int numberDisk){
        //variaveis locais
        List<Byte> fileParity = new ArrayList();

        //CREA LAS LISTAS
        List<List<Byte>> superlist = new ArrayList<List<Byte>>();
        for (int i = 0; i < numberDisk; i++) {
            superlist.add(new ArrayList());
        }

        int countFile=numberDisk-1;
        for(byte element : bytesArray) {
            superlist.get(countFile).add(element);
            countFile--;
            if (countFile==-1){
                countFile=numberDisk-1;
            }
        }

        int major = getMajorSize(superlist);
        completeDisks(superlist,major);


        byte subtraction = 0;
        for (int i = 0; i < superlist.get(superlist.size()-1).size(); i++){
            subtraction = superlist.get(superlist.size()-1).get(i);

            for (int j = numberDisk-1; j > 0; j--) {
                subtraction= (byte) (subtraction-superlist.get(j-1).get(i));
            }

            fileParity.add(subtraction);
        }

        for (int i = 0; i < numberDisk; i++) {
            fillBytesFile(superlist.get(i), new File(PATH + "DISK"+i, "file"+i+".txt"));
            fillBytesFile(fileParity, new File(PATH + "DISK"+i, FILEPARITY));
        }
    }

    private static void completeDisks(List<List<Byte>> superlist, int major) {
        int substraction;

        for (int i = 0; i < superlist.size(); i++) {
            substraction = major - superlist.get(i).size();
            if(substraction!=0){
                for (int j = 0; j < substraction; j++) {
                    superlist.get(i).add((byte) 0);
                }
            }
        }
    }

    private static int getMajorSize(List<List<Byte>> superlist) {
        int major=superlist.get(0).size();
        for (int i = 1; i < superlist.size(); i++) {
            if(major<superlist.get(i).size()){
                major=superlist.get(i).size();
            }
        }
        return major;
    }

    //cria arquivo como byte. e chamado pelo saveFileWithRAID5 REFATORADO
    private static void fillBytesFile(List<Byte> bytesArray, File file){
        try {
            w = new PrintWriter(file);
            for (byte value : bytesArray) w.println(value);
            w.close();
            System.out.printf("Arquivo %s criado com sucesso!\n", file.getName());
        } catch (FileNotFoundException e) {
            System.out.println("Não foi possível criar o arquivo solicitado!");
        }
    }

    //Apaga um disco para teste de recuperação da paridade
    private static void deleteDisk(String disk){

        File diskToDelete = new File(PATH + disk);

        String[] contents = diskToDelete.list();
        for (String content : contents){
            new File(diskToDelete, content).delete();
        }
        diskToDelete.delete();
    }

    //Recupera o disco com o disco de paridade REFATORADO
    private static void findDiskCorrupted(List<String> disks){
        int countDiskCorrupted = countDiskCorrupted(disks);
        List<Integer> filesForBackUp = new ArrayList<>();

        if (countDiskCorrupted==1){
            for (int i = 0; i < disks.size(); i++) {
                if (!new File(PATH+disks.get(i)).exists()) {
                    for (int j = 0; j < disks.size(); j++) {
                        if(j!=i){
                            filesForBackUp.add(j);
                        }
                    }
                    recoveryDisk(disks.get(i), filesForBackUp);
                    break;
                }
            }
        }else if(countDiskCorrupted>=2){
            System.out.println("Imposible recuperar el archivo, hay mas de un disco dañado!");
        }else{
            System.out.println("Ningun disco está dañado!");
        }
    }

    private static int countDiskCorrupted(List<String> disks) {
        int countCorrupted = 0;
        for (int i = 0; i < disks.size(); i++) {
            if (!new File(PATH+disks.get(i)).exists())
                countCorrupted++;
        }
        return countCorrupted;
    }

    private static void recoveryDisk(String diskLose, List<Integer> numberFileBackUp){
        try {
            //Recrear el disco
            createDisks(Arrays.asList(diskLose));
            //Recrea el archivo dañado
            File recoveredFile = new File(PATH + diskLose, FILERECOVERED);
            //Disco existente
            DISKDamaged = diskLose;

            List<Scanner> scanners = new ArrayList<>();
            for (int i = 0; i < numberFileBackUp.size(); i++) {
                scanners.add(new Scanner(new FileReader(new File(PATH + "DISK"+numberFileBackUp.get(i), "file"+numberFileBackUp.get(i)+".txt"))).useDelimiter("\\n"));
            }

            //Get a ramdon disk number for recover parity
            Random random = new Random();
            Integer numberRandom = numberFileBackUp.get(random.nextInt(numberFileBackUp.size()));

            //Cria um leitor do arquivo de paridade para criar a paridade
            Scanner readFileParity = new Scanner(new FileReader(new File(PATH + "DISK"+numberRandom, FILEPARITY))).useDelimiter("\\n");

            List<Byte> bytesArrayFile = new ArrayList();
            while (readFileParity.hasNext()) {
                List<Byte> bytesForRecover = new ArrayList();

                for (int i = numberFileBackUp.size()-1; i > -1 ; i--) {
                    if(!DISKDamaged.equals(lastDisk)){
                        bytesForRecover.add((scanners.get(i).hasNext()) ? Byte.parseByte(scanners.get(i).next().replace("\r", "")) : 0);
                    }else{
                        bytesForRecover.add((scanners.get(i).hasNext()) ? (byte) (0 - Byte.parseByte(scanners.get(i).next().replace("\r", ""))) : 0);
                    }
                }

                bytesForRecover.add(Byte.parseByte(readFileParity.next().replace("\r", "")));
                bytesArrayFile.add(recoveredByte(bytesForRecover));
            }

            fillBytesFile(bytesArrayFile, recoveredFile);
            System.out.println("\nRecuperando arquivo!");
            System.out.println("Arquivo recuperado com sucesso!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Byte recoveredByte(List<Byte> bytesForRecover) {
        byte result=0;

        int finalIndex = bytesForRecover.size()-1;

        if(DISKDamaged.equals(lastDisk)){
            for (int i = 0; i < bytesForRecover.size()-1; i++) {
                result = (byte) (result - bytesForRecover.get(i));
            }
            result = (byte) (result + bytesForRecover.get(finalIndex));
        }else{
            for (int i = 0; i < bytesForRecover.size()-1; i++) {
                if (i == 0){
                    result = (byte) (result + bytesForRecover.get(i));
                }else{
                    result = (byte) (result - bytesForRecover.get(i));
                }
            }
            byte byteRecover = (byte) (0 - bytesForRecover.get(finalIndex));
            result = (byte) (result + byteRecover);
        }
        return result;
    }

    //Remonta os arquivo
    private static void remakeFile(List<String> disks) {
        boolean flag= true;
        int countFile=disks.size()-1;
        List<Byte> fullFile = new ArrayList();
        byte[] fullFileArray;

        List<String[]> contents = new ArrayList<>();
        for (int i = 0; i < disks.size(); i++) {
            contents.add(new File(PATH + disks.get(i)).list());
        }

        try {
            List<Scanner> scanners = new ArrayList<>();
            for (int i = 0; i < disks.size(); i++) {
                scanners.add(new Scanner(new File(PATH + disks.get(i), contents.get(i)[0])));
            }

            while (flag){
                fullFile.add(Byte.parseByte(scanners.get(countFile).next()));
                countFile--;
                if (countFile == -1) {
                    countFile = disks.size() - 1;
                }
                for (int i = 0; i < scanners.size(); i++) {
                    flag = scanners.get(i).hasNext()? true: false;
                    if(flag){
                        break;
                    }
                }
            }
            fullFileArray = new byte[fullFile.size()];
            for (int i = 0; i < fullFile.size(); i++){
                fullFileArray[i] = fullFile.get(i);
            }
            String s = new String(fullFileArray);

            File remakeFile = new File(PATH, "HinoRecuperado.txt");
            PrintWriter w = new PrintWriter(remakeFile);
            w.print(s);
            w.close();

            System.out.println("\nArquivo...\n");
            System.out.println(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //endregion

    public static void main(String[] args) throws FileNotFoundException {
//        DISK.add("DISK0");
//        DISK.add("DISK1");
//        DISK.add("DISK2");
//        DISK.add("DISK3");
//        DISK.add("DISK4");
//        DISK.add("DISK5");
//        DISK.add("DISK6");
//        DISK.add("DISK7");
//        DISK.add("DISK8");
//
//        lastDisk = DISK.get(DISK.size()-1);
//        createDisks(DISK);
//        byte[] fileLikeByte = parseFileToByte(new File(PATH + "Hino.txt"));
//        saveFileWithRAID5(fileLikeByte,DISK.size());
//        deleteDisk("DISK5");
//        findDiskCorrupted(DISK);
//        remakeFile(DISK);

        try {
            Send send= new Send();

            //send.sendFile(new File("C:\\21-IF5000-RAID5\\Processos_Disk_Nodes_Java\\files\\PRUEBA.txt"));
            //send.sendFile(new File("C:\\21-IF5000-RAID5\\Processos_Disk_Nodes_Java\\files\\MAMAHUEVO.txt"));
            send.getFile("MAMAHUEVO.txt");
            send.getFile("PRUEBA.txt");
        } catch (Exception e) {
            throw new RuntimeException("FAIL!");
        }


    }
}

