package com.example.apiclient.model;

import javax.persistence.*;

@Entity
@Table(name = "Client")
public class Client {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clientId;
    private String name;
    private String password;

    public Client(int clientId, String name,  String password) {
        this.clientId= clientId;
        this.name = name;
        this.password = password;

    }

    public Client() {
    }


    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
