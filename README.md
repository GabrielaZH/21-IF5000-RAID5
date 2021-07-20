
# 21-IF500-TFTP
*** 
saSEARCH is a client application that connects to Controller Node to find the books and metadata on the RAID. This application requires entering the words of the search argument and returns the list of books that match the words entered using the metadata as a base. In addition, according to the search result, it shows the list of books and displays their content when any of these are selected.
*** 
### Application functionalities:

**Angular cli (FrontEnd_Angular):** 

- allows to list the books (plain text files)
- allows to request the file
- allows uploading files

**Springboot Api (Client_API_SpringBoot):**

- Allows save allows to save the metadata of the books in the SQL data base
- Communicates with the nodeController to send and receive files


**Java application (Controller_Node_Java):**

- It receives the client's requests which are to send and receive files.
- Controls the access and storage of books in the nodes

**Java application (Processos_Disk_Nodes_Java):**

- It receives requests which are to send and receive files from nodeController.


**SQL (DataBase):**

- Stores books data.


*** 
### Execution requirements:

- Save proyect in "C:" disk.
- SQL Management Studio 15.0.18369.0
- IntelliJ IDEA 2021.1.3 
- Configured a server (Tomcat preferably)
- Environment variables configured for Java (jre1.8.0_231 y jdk1.8.0_231)
- Java configured jdk1.8.0_261
- Angular cli 12.0.1
- Visual Studio Code 1.56.2


*** 
### To run proyect:

**SQL:**

- Open SQL Management Studio and execute ScriptDB.sql file


**Springboot Api (Client_API_SpringBoot):**

- Open proyect using IntelliJ IDEA 2021.1.3 
- Run using the server Tomcat configurations

**Java application (Controller_Node_Java):**

- Open proyect using IntelliJ IDEA 2021.1.3 
- Run main class

**Java application (Processos_Disk_Nodes_Java):**

- Open proyect using IntelliJ IDEA 2021.1.3 
- Run main class

**Angular cli (FrontEnd_Angular_Client):**

- Open proyect in Visual Studio Code. Use the terminal and add modules with this commands and
the last command run the proyect:
```
$ npm install -g @angular-devkit/schematics-cli

$ npm install --save-dev @angular-devkit/build-angular

$ ng serve
```
- Open proyect in browser
- Select number of nodes
- Upload one file
- Choose book to see

***
support material used in this project: 
[Huffman](https://github.com/JaimePerezS/Codificacion-Huffman/blob/master/README.md).
[Client-Server](https://github.com/Rytiggy/Chat-Client-Server/blob/master/Chat-Client-Server.zip).
[Raid](https://github.com/andreiAndrade/Raid5)

***
