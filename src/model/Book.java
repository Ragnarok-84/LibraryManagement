package model;

public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final int year;
    private final int totalCopies;
    private int availableCopies;
    private int borrowedCount;

    public Book(String isbn, String title, String author, int year, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.borrowedCount = 0;
    }

    public Book(String isbn, String title, String author, int year, int totalCopies, int availableCopies, int borrowedCount) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.borrowedCount = borrowedCount;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public int getBorrowedCount() { return borrowedCount; }

    public void borrow() {
        if (availableCopies > 0) {
            availableCopies--;
            borrowedCount++;
        }
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s, %d) | Sẵn có: %d/%d | Đã mượn: %d",
                isbn, title, author, year, availableCopies, totalCopies, borrowedCount);
    }
}
