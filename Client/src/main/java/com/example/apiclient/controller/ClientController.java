package com.example.apiclient.controller;




import com.example.apiclient.model.Client;
import com.example.apiclient.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "/api/client")
@RestController
public class ClientController {

    @Autowired private ClientService service;

    @GetMapping("/clients")
    public List<Client> getAll() {
        return service.listAll();
    }

    @RequestMapping(value = "/uploadFile")
    public ResponseEntity<?> uploadFile(/*@RequestParam("file") MultipartFile file*/) throws FileNotFoundException {

        Map<String,Object> response = new HashMap<>();

        Send send= new Send();
        send.sendFile("C:\\ar.txt");


        return new ResponseEntity<Map<String, Object>>( response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/receiveFile")
    public ResponseEntity<?> receiveFile(/*@RequestParam("file") MultipartFile file*/) throws FileNotFoundException {

        Map<String,Object> response = new HashMap<>();
        Send send= new Send();
        send.getFile("text1.txt");

        return new ResponseEntity<Map<String, Object>>( response, HttpStatus.CREATED);
    }
}
