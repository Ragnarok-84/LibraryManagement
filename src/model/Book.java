package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private int year;
    private int total;
    private int available;
    private int borrowedCount;
    
    // Các trường bổ sung từ database
    private String isbn13;
    private String languageCode;
    private int numPages;
    private BigDecimal averageRating;
    private int ratingsCount;
    private int textReviewsCount;
    private LocalDate publicationDate;

    public Book() {
    }

    public Book(String isbn, String title, String author, int year, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.total = totalCopies;
        this.available = totalCopies;
        this.borrowedCount = 0;
    }

    public Book(String isbn, String title, String author, int year, int totalCopies, int availableCopies, int borrowedCount) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.total = totalCopies;
        this.available = availableCopies;
        this.borrowedCount = borrowedCount;
    }

    // UI-style getters/setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }

    public int getBorrowedCount() { return borrowedCount; }
    public void setBorrowedCount(int borrowedCount) { this.borrowedCount = borrowedCount; }

    // Các getter/setter cho trường bổ sung
    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public int getNumPages() { return numPages; }
    public void setNumPages(int numPages) { this.numPages = numPages; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public int getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }

    public int getTextReviewsCount() { return textReviewsCount; }
    public void setTextReviewsCount(int textReviewsCount) { this.textReviewsCount = textReviewsCount; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    // Backward-compatible aliases for DAO
    public int getTotalCopies() { return total; }
    public int getAvailableCopies() { return available; }

    public void borrow() {
        if (available > 0) {
            available--;
            borrowedCount++;
        }
    }

    public void returnBook() {
        if (available < total) {
            available++;
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s, %d) | Sẵn có: %d/%d | Đã mượn: %d",
                isbn, title, author, year, available, total, borrowedCount);
    }
}
