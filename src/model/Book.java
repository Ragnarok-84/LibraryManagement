package model;

public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final int year;
    private int availableCopies;
    private int borrowedCount;

    public Book(String isbn, String title, String author, int year, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.availableCopies = availableCopies;
        this.borrowedCount = 0;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public int getAvailableCopies() { return availableCopies; }
    public int getBorrowedCount() { return borrowedCount; }

    public void borrow() {
        if (availableCopies > 0) {
            availableCopies--;
            borrowedCount++;
        }
    }

    public void returnBook() {
        availableCopies++;
    }

    @Override
    public String toString() {
        return isbn + " - " + title + " (" + author + ", " + year + "), Sẵn có: " + availableCopies;
    }
}
