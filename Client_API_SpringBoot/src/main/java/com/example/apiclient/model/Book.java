package com.example.apiclient.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "Book")
public class Book {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Date date;
    private String author;
    private Short nodes;


    public Book(int id, String name, Date date, String author, Short nodes) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.author = author;
        this.nodes = nodes;
    }


    public Book() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Short getNodes() {
        return nodes;
    }

    public void setNodes(Short nodes) {
        this.nodes = nodes;
    }

}
