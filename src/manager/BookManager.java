package manager;

import dao.BookDAO;
import model.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lop quan ly logic nghiep vu cho Book.
 * Trien khai Searchable va Reportable de cung cap cac chuc nang tim kiem va bao cao.
 * Su dung BookDAO de thuc hien cac thao tac du lieu.
 */
public class BookManager implements Searchable<Book>, Reportable<Book> {

    // Dependency Injection: Su dung interface BookDAO de dam bao tinh linh hoat
    private final BookDAO bookDAO;

    public BookManager(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    // --- Chuc nang quan ly co ban (CRUD duoc chuyen tu in-memory map sang DAO) ---

    public void addBook(Book newBook) {
        System.out.println("Dang them sach: " + newBook.getTitle() + " qua DAO.");
        bookDAO.add(newBook);
    }

    public void removeBook(String isbn) {
        Book bookToRemove = findBookByIsbn(isbn);
        if (bookToRemove != null) {
            System.out.println("Da xoa sach co ISBN " + isbn);
            bookDAO.delete(bookToRemove.getBookID());
        } else {
            System.out.println("Khong tim thay sach co ISBN nay.");
            return ;
        }
    }

    public Book findBookByIsbn(String isbn) {
        return bookDAO.findAll().stream()
                .filter(b -> b.getIsbn() != null && b.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }


    // --- Implement Searchable<Book> ---

    @Override
    public List<Book> search(String query) {
        List<Book> allBooks = bookDAO.findAll();
        String lowerCaseQuery = query.toLowerCase();

        return allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Book> searchById(int id) {
        return bookDAO.findByID(id);
    }

    // --- Implement Reportable<Book> ---

    @Override
    public List<Book> generateGeneralReport() {

        final double MIN_RATING = 3.5;

        return bookDAO.findAll().stream()
                .filter(b -> b.getAverageRating() >= MIN_RATING)
                .collect(Collectors.toList());


    }

    @Override
    public Map<String, Long> generateStatisticalReport(String criteria) {
        List<Book> allBooks = bookDAO.findAll();

        // Thong ke so luong sach theo cac tieu chi co san trong CSV
        if ("publisher".equalsIgnoreCase(criteria)) {
            // Thong ke theo nha xuat ban (Publisher)
            return allBooks.stream()
                    .collect(Collectors.groupingBy(Book::getPublisher, Collectors.counting()));
        } else if ("authors".equalsIgnoreCase(criteria)) {
            // Thong ke theo tac gia. Luu y: se nhom theo ca chuoi tac gia (vi du: "A/B" la mot nhom rieng).
            return allBooks.stream()
                    .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));
        } else if ("language_code".equalsIgnoreCase(criteria)) {
            // Thong ke theo ma ngon ngu
            return allBooks.stream()
                    .collect(Collectors.groupingBy(Book::getLanguageCode, Collectors.counting()));
        }

        // Tra ve map bao loi neu tieu chi khong hop le
        return Map.of("Error: Criteria not supported (Try 'publisher', 'authors', or 'language_code')", 0L);
    }

    /*
    @Override
    public List<Book> generateFilteredReport(Map<String, String> filters) {
        // Day la mot phuong thuc phuc tap, chi de lai phan khung.
        // Can trien khai logic xu ly Map<String, String> filters de loc du lieu.
        return bookDAO.findAll();
    }*/
}
