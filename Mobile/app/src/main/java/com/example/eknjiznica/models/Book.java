package com.example.eknjiznica.models;

import java.io.Serializable;

public class Book implements Serializable {
    private int id;
    private String title;
    private String author;
    private int year;
    private String genre;
    private boolean isAvailable;

    public Book() {
    }

    public Book(int id, String title, String author, int year, String genre, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
