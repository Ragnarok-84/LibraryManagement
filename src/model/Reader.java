package model;

import java.util.ArrayList;
import java.util.List;

public class Reader {
    private final int id;
    private final String name;
    private final String email;
    private final List<Book> borrowedBooks;

    public Reader(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.borrowedBooks = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void borrowBook(Book book) { borrowedBooks.add(book); }
    public void returnBook(Book book) { borrowedBooks.remove(book); }

    public List<Book> getBorrowedBooks() { return borrowedBooks; }

    @Override
    public String toString() {
        return id + " - " + name;
    }


}
