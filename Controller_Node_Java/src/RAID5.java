import java.io.*;
import java.util.*;

public class RAID5 {


    /*Crea carpetas temporales de discos donde iran el archivo con el trozo del  libro y su paridad*/
    public void createDisks(List<String> disks, String path){
        File disk;
        for (String name : disks){
            disk = new File(path, name);
            if (!disk.exists()){
                if (!disk.mkdirs()) System.out.printf("%s não pode ser criado!\n", name);
            }
        }
    }

    public byte[] parseFileToByte(File file){
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

    public void saveFileWithRAID5(byte[] bytesArray, int numberDisk, String path){
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
            fillBytesFile(superlist.get(i), new File(path + "DISK"+i, "file"+i+".txt"));
            fillBytesFile(fileParity, new File(path + "DISK"+i, "fileParity"+i+".txt"));
        }
    }

    //Recupera o disco com o disco de paridade REFATORADO
    public int findDiskCorrupted(List<String> disks,String path, String lastDisk){
        int countDiskCorrupted = countDiskCorrupted(disks,path);
        List<Integer> filesForBackUp = new ArrayList<>();

        if (countDiskCorrupted==1){
            for (int i = 0; i < disks.size(); i++) {
                if (!new File(path+disks.get(i)).exists()) {
                    for (int j = 0; j < disks.size(); j++) {
                        if(j!=i){
                            filesForBackUp.add(j);
                        }
                    }
                    recoveryDisk(disks.get(i), filesForBackUp, path, lastDisk);
                    break;
                }
            }
        }else if(countDiskCorrupted>=2){
            System.out.println("Imposible recuperar el archivo, hay mas de un disco dañado!");
        }else{
            System.out.println("Ningun disco está dañado!");
        }
        return countDiskCorrupted;
    }

    public void remakeFile(List<String> disks, String path, String fileRestored) {
        boolean flag= true;
        int countFile=disks.size()-1;
        List<Byte> fullFile = new ArrayList();
        byte[] fullFileArray;

        List<String[]> contents = new ArrayList<>();
        for (int i = 0; i < disks.size(); i++) {
            contents.add(new File(path + disks.get(i)).list());
        }

        try {
            List<Scanner> scanners = new ArrayList<>();
            for (int i = 0; i < disks.size(); i++) {
                scanners.add(new Scanner(new File(path + disks.get(i), contents.get(i)[0])));
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

            File remakeFile = new File(path, fileRestored);
            PrintWriter w = new PrintWriter(remakeFile);
            w.print(s);
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void recoveryDisk(String diskLose, List<Integer> numberFileBackUp, String path, String lastDisk){
        try {
            //Recrear el disco
            createDisks(Arrays.asList(diskLose),path);
            int numberDiskLose = Integer.parseInt(diskLose.substring(4));
            //Recrea el archivo dañado
            File recoveredFile = new File(path + diskLose, "file"+numberDiskLose+".txt");


            List<Scanner> scanners = new ArrayList<>();
            for (int i = 0; i < numberFileBackUp.size(); i++) {
                scanners.add(new Scanner(new FileReader(new File(path + "DISK"+numberFileBackUp.get(i), "file"+numberFileBackUp.get(i)+".txt"))).useDelimiter("\\n"));
            }

            //Get a ramdon disk number for recover parity
            Random random = new Random();
            Integer numberRandom = numberFileBackUp.get(random.nextInt(numberFileBackUp.size()));

            //Cria um leitor do arquivo de paridade para criar a paridade
            Scanner readFileParity = new Scanner(new FileReader(new File(path + "DISK"+numberRandom, "fileParity"+numberRandom+".txt"))).useDelimiter("\\n");

            List<Byte> bytesArrayFile = new ArrayList();
            while (readFileParity.hasNext()) {
                List<Byte> bytesForRecover = new ArrayList();

                for (int i = numberFileBackUp.size()-1; i > -1 ; i--) {
                    if(!diskLose.equals(lastDisk)){
                        bytesForRecover.add((scanners.get(i).hasNext()) ? Byte.parseByte(scanners.get(i).next().replace("\r", "")) : 0);
                    }else{
                        bytesForRecover.add((scanners.get(i).hasNext()) ? (byte) (0 - Byte.parseByte(scanners.get(i).next().replace("\r", ""))) : 0);
                    }
                }

                bytesForRecover.add(Byte.parseByte(readFileParity.next().replace("\r", "")));
                bytesArrayFile.add(recoveredByte(bytesForRecover, diskLose, lastDisk));
            }

            fillBytesFile(bytesArrayFile, recoveredFile);
            System.out.println("\nRecuperando arquivo!");
            System.out.println("Arquivo recuperado com sucesso!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Byte recoveredByte(List<Byte> bytesForRecover, String diskLose, String lastDisk) {
        byte result=0;

        int finalIndex = bytesForRecover.size()-1;

        if(diskLose.equals(lastDisk)){
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

    public int countDiskCorrupted(List<String> disks, String path) {
        int countCorrupted = 0;
        for (int i = 0; i < disks.size(); i++) {
            if (!new File(path+disks.get(i)).exists())
                countCorrupted++;
        }
        return countCorrupted;
    }

    public void completeDisks(List<List<Byte>> superlist, int major) {
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

    public int getMajorSize(List<List<Byte>> superlist) {
        int major=superlist.get(0).size();
        for (int i = 1; i < superlist.size(); i++) {
            if(major<superlist.get(i).size()){
                major=superlist.get(i).size();
            }
        }
        return major;
    }

    public void fillBytesFile(List<Byte> bytesArray, File file){
        try {
            PrintWriter w;
            w = new PrintWriter(file);
            for (byte value : bytesArray) w.println(value);
            w.close();
        } catch (FileNotFoundException e) {
            System.out.println("No es posible crear el archivo deseado!");
        }
    }
}
