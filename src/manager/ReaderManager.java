package manager;

import dao.ReaderDAO;
import model.Reader;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReaderManager implements Searchable<Reader>, Reportable<Reader> {

    private final ReaderDAO readerDAO;

    public ReaderManager(ReaderDAO readerDAO) {
        this.readerDAO = readerDAO;
    }

    public void addReader(Reader reader) {
        // Them logic kiem tra du lieu dau vao hop le tai day.
        System.out.println("Dang them doc gia: " + reader.getName() + " qua DAO.");
        readerDAO.add(reader);
    }

    public void removeReader(int id) {
        System.out.println("Da xoa doc gia co ID " + id);
        readerDAO.delete(id);
    }

    public Optional<Reader> findReaderById(int id) {
        return readerDAO.findByID(id);
    }

    public List<Reader> getAllReaders() {
        return readerDAO.findAll();
    }

    @Override
    public List<Reader> search(String query) {
        // Tim kiem doc gia theo ten
        String lowerCaseQuery = query.toLowerCase();

        return readerDAO.findAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reader> searchById(int id) {
        return readerDAO.findByID(id);
    }

    // --- Implement Reportable<Reader> ---

    @Override
    public List<Reader> generateGeneralReport() {
        // Bao cao: Danh sach tat ca doc gia dang hoat dong (isActive == true)
        return readerDAO.findAll().stream()
                .filter(Reader::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> generateStatisticalReport(String criteria) {
        // Thong ke so luong doc gia theo trang thai thanh vien (Membership Status)
        if ("membership".equalsIgnoreCase(criteria)) {
            return readerDAO.findAll().stream()
                    .collect(Collectors.groupingBy(r -> String.valueOf(r.isActive()), Collectors.counting()));
        }
        return Map.of("Error", 0L);
    }
}
