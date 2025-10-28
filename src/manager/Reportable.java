package manager;

import java.util.List;
import java.util.Map;

public interface Reportable<T> {


    List<T> generateGeneralReport();
    Map<String, Long> generateStatisticalReport(String criteria);
    //List<T> generateFilteredReport(Map<String, String> filters);
}
