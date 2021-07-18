package com.example.apiclient.controller;

import com.example.apiclient.model.Book;
import com.example.apiclient.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


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

    @RequestMapping(value = "/uploadFile")
    public ResponseEntity<?> uploadFile(/*@RequestParam("file") MultipartFile file, String numNodes*/) {
        Map<String, Object> response = new HashMap<>();
        try {
            try {

                Send send = new Send();

                //MultipartFile multipartFile = new MockMultipartFile((file.getOriginalFilename()), file.getInputStream());
                File file = new File("C:\\are.txt");
                FileInputStream inputStream = new FileInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
                send.sendFile(multipartFile, "16");
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

    @RequestMapping(value = "/receiveFile")
    public ResponseEntity<?> receiveFile(/*string file*/) throws IOException {

        Map<String, Object> response = new HashMap<>();
        Send send = new Send();

        File fileReceived = send.getFile("are.txt");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

}


