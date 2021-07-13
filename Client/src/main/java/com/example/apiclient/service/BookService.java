package com.example.apiclient.service;

import com.example.apiclient.model.Book;
import com.example.apiclient.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;


@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository repository;


    public List<Book> listAll(){return repository.findAll();}
    public Book get (int id){ return repository.findById(id).get();}
    public void delete(int id){repository.deleteById(id);}


}
