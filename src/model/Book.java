package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Book {
    private String bookId; // <--- Đã thêm trường bookID
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int numPages;

    // Các trường mặc định cho thư viện, không có trong CSV
    private String category;
    private int year;
    private int total;
    private int available;
    private int borrowedCount;

    // Các trường bổ sung từ database/CSV
    private String isbn13;
    private String languageCode;
    private BigDecimal averageRating;
    private int ratingsCount;
    private int textReviewsCount;
    private LocalDate publicationDate;

    // --- Constructors ---

    public Book() {
        // Constructor mặc định
    }

    // Constructor cơ bản cho thư viện (như ban đầu)
    public Book(String isbn, String title, String author, int year, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.total = totalCopies;
        this.available = totalCopies;
        this.borrowedCount = 0;
    }

    // Constructor chi tiết cho thư viện (như ban đầu)
    public Book(String isbn, String title, String author, int year, int totalCopies, int availableCopies, int borrowedCount) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.total = totalCopies;
        this.available = availableCopies;
        this.borrowedCount = borrowedCount;
    }

    // --- Static Factory Method để Parse CSV ---

    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    /**
     * Tạo một đối tượng Book từ một dòng dữ liệu CSV.
     * Thứ tự cột: bookID,title,authors,average_rating,isbn,isbn13,language_code,num_pages,ratings_count,text_reviews_count,publication_date,publisher
     */
    public static Book fromCsvLine(String csvLine) {
        // Sử dụng regex để tách chuỗi, cố gắng xử lý các trường có thể chứa dấu phẩy bên trong ("...")
        List<String> fields = Arrays.asList(csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));

        if (fields.size() < 12) {
            System.err.println("Dòng CSV không hợp lệ: " + csvLine);
            return null;
        }

        Book book = new Book();

        // bookID (index 0) - Đã thêm
        book.setBookId(fields.get(0).trim());

        book.setTitle(fields.get(1).trim().replace("\"", "")); // title (index 1)
        book.setAuthor(fields.get(2).trim().replace("\"", "")); // authors (index 2)

        // average_rating (index 3)
        try {
            book.setAverageRating(new BigDecimal(fields.get(3).trim()));
        } catch (NumberFormatException e) {
            book.setAverageRating(BigDecimal.ZERO);
        }

        book.setIsbn(fields.get(4).trim()); // isbn (index 4)
        book.setIsbn13(fields.get(5).trim()); // isbn13 (index 5)
        book.setLanguageCode(fields.get(6).trim()); // language_code (index 6)

        // num_pages (index 7)
        try {
            book.setNumPages(Integer.parseInt(fields.get(7).trim()));
        } catch (NumberFormatException e) {
            book.setNumPages(0);
        }

        // ratings_count (index 8)
        try {
            book.setRatingsCount(Integer.parseInt(fields.get(8).trim()));
        } catch (NumberFormatException e) {
            book.setRatingsCount(0);
        }

        // text_reviews_count (index 9)
        try {
            book.setTextReviewsCount(Integer.parseInt(fields.get(9).trim()));
        } catch (NumberFormatException e) {
            book.setTextReviewsCount(0);
        }

        // publication_date (index 10)
        try {
            book.setPublicationDate(LocalDate.parse(fields.get(10).trim(), CSV_DATE_FORMATTER));
        } catch (Exception e) {
            book.setPublicationDate(null);
        }

        book.setPublisher(fields.get(11).trim()); // publisher (index 11)

        // Thiết lập các giá trị mặc định cho thư viện
        book.setYear(book.getPublicationDate() != null ? book.getPublicationDate().getYear() : 0);
        book.setTotal(1);
        book.setAvailable(1);
        book.setBorrowedCount(0);

        return book;
    }

    // --- Getters/Setters ---

    // Getter/Setter cho bookID
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    // Các Getter/Setter khác (giữ nguyên)
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

    // --- Backward-compatible aliases for DAO ---
    public int getTotalCopies() { return total; }
    public int getAvailableCopies() { return available; }

    // --- Business Logic ---

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
        return String.format("ID %s | %s - %s (%s, %d) | Sẵn có: %d/%d | Đã mượn: %d | Rating: %.2f",
                bookId, isbn, title, author, year, available, total, borrowedCount, averageRating != null ? averageRating.doubleValue() : 0.0);
    }
}