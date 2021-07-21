package com.example.apiclient.controller;

import com.example.apiclient.model.Book;
import com.example.apiclient.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;

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
                book.setAuthor(file.getName());
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
    public ResponseEntity<?> receiveFile(@RequestParam("file") String file, @RequestParam("numNodes")String numNodes) throws IOException {

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

            File f= new File("C:\\FUENTEOVEJUNA LOPE DE VEGA.txt");
            Path path = f.toPath();


            Resource resource = new UrlResource(path.toUri());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("File-Name", f.getName());
            httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                    .headers(httpHeaders).body(resource);

        } catch (Exception e) {
            response.put("message", e.getMessage().concat(":").concat(e.getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CONFLICT);
        }
    }

}


