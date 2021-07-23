package com.example.apiclient.controller;

import com.example.apiclient.model.Book;
import com.example.apiclient.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "/api/client")
@RestController
public class ClientController {

    @Autowired
    private BookService service;

    @GetMapping("/books")
    public List<Book> getAll() {
        return service.listAll();
    }
    InetAddress currentClientIP;

    /**
     * Receive file from FrontEnd_Angular and upload to Controller_Node_Java
     * @param file file is the received
     * @param numNodes num nodes received
     * @return ResponseEntity with status
     */
    @RequestMapping(value = "/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam("numNodes") String numNodes ) {
        Map<String, Object> response = new HashMap<>();
        try {
            try {

                Send send = new Send();
                send.sendFile(file, numNodes);
                Book book= new Book();
                book.setName(file.getOriginalFilename());

                String owner = InetAddress.getLocalHost()+"";
                owner = owner.substring(0,owner.indexOf("/"));

                book.setAuthor(owner);
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sDate = new java.sql.Date(utilDate.getTime());
                book.setDate(sDate);
                book.setNodes(Short. parseShort(numNodes));
                service.save(book);

            } catch (Exception e) {
                throw new RuntimeException("FAIL!");
            }
            response.put("message", "Successfully uploaded!");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.getMessage().concat(":").concat(e.getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CONFLICT);
        }

    }

    /**
     * receives files from  Controller_Node_Java
     * @param file file is the received
     * @param numNodes num nodes received
     * @return ResponseEntity with file
     * @throws IOException Exception
     */
    @RequestMapping(value = "/receiveFile")
    @Async
    public  ResponseEntity<?> receiveFile(@RequestParam("file") String file, @RequestParam("numNodes")String numNodes) throws IOException, InterruptedException {
        Map<String, Object> response = new HashMap<>();
        File filereceive ;
        try {
            try {

                Send send = new Send();
                filereceive = send.getFile(file+".txt",numNodes);

            } catch (Exception e) {
                throw new RuntimeException("FAIL!");
            }
            response.put("message", "Successfully downloaded!");
           // File file1 = new File("C:\\21-IF5000-RAID5\\Client_API_SpringBoot\\filesReceive\\La vida es sueño - Calderón de la Barca.txt.txt");
            Path path = filereceive.toPath();

            Thread.sleep(10000L);

            Resource resource = new UrlResource(path.toUri());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("File-Name", filereceive.getName());
            httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                    .headers(httpHeaders).body(resource);

        } catch (Exception e) {
            response.put("message", e.getMessage().concat(":").concat(e.getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CONFLICT);
        }
    }

}


