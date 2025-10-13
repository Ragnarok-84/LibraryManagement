package test;

import dao.BookDAO;
import model.Book;

public class Main {
    public static void main(String[] args) {
        BookDAO dao = new BookDAO();

        // thêm sách
        Book book = new Book("123456", "Lập trình Java", "Nguyễn Văn A", 2024, 5);
        dao.addBook(book);

        // in toàn bộ sách
        dao.getAllBooks().forEach(System.out::println);

        // tìm sách theo ISBN
        Book found = dao.findByISBN("123456");
        System.out.println("Tìm được: " + found);
    }
}
