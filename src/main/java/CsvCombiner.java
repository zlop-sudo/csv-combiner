import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class is a command line based program, the main entrance of the csv combiner.
 * @author Qiaodan Zhao
 * @version 1.1
 */
public class CsvCombiner {
    public static void main(String[] args) {
        String[] inputFileNames = args;
        Combiner combiner = new Combiner();
        combiner.combine(inputFileNames);
    }
}

/**
 * This class contains all methods needed for combing the csv files with different columns/headers.
 * It will set the different columns' value to null accordingly.
 */
class Combiner {
    public Combiner() {
    }

    /**
     * This method is the main entrance of the csv combiner.
     * It uses other four methods to compute and print the combined csv to std out.
     * @param inputFileNames The array of the input csv files' names.
     */
    public void combine(String[] inputFileNames) {
        Pattern r = Pattern.compile(".+.csv");
        for (String fileName : inputFileNames) {
            Matcher matcher = r.matcher(fileName);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Input file must be csv format!");
            }
        }
        List<List<String>> hearders = getCsvHeaders(inputFileNames);
        List<String> combinedHeaders = combineHeaders(hearders);

        writeCombinedHeader(combinedHeaders);
        for (int i = 0; i < inputFileNames.length; i++) {
            writeCombinedCsv(inputFileNames[i], hearders.get(i), combinedHeaders);
        }
    }

    /**
     * This method read an array of file names and get their csv headers from the first line.
     * @param inputFileNames The array of the input csv files' names.
     * @return List of every csv file's headers.
     */
    public List<List<String>> getCsvHeaders(String[] inputFileNames) {
        List<List<String>> headers = new ArrayList<>();

        for (String inputFileName : inputFileNames) {
            List<String> header = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
                String[] firstLine = br.readLine().split(",");
                header = Arrays.asList(firstLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            headers.add(header);
        }

        return headers;
    }

    /**
     * This method merges all csv files' headers into one in order.
     * @param hearders List of every csv file's headers.
     * @return List of combined csv file's headers.
     */
    public List<String> combineHeaders(List<List<String>> hearders) {
        List<String> combinedHeaders = new ArrayList<>();
        Set<String> set = new HashSet<>();

        for (List<String> header : hearders) {
            for (String col : header) {
                if (!set.contains(col)) {
                    combinedHeaders.add(col);
                    set.add(col);
                }
            }
        }
        combinedHeaders.add("\"filename\"");

        return combinedHeaders;
    }

    /**
     * This method is to print the combined csv file's headers to std out.
     * @param combinedHeaders the merged headers for the result file
     */
    public void writeCombinedHeader(List<String> combinedHeaders) {
        for (int i = 0; i < combinedHeaders.size(); i++) {
            System.out.print(combinedHeaders.get(i));
            if (i != combinedHeaders.size() - 1) {
                System.out.print(",");
            }
        }
    }

    /**
     * This method is to print the combined csv file's data to std out and set the null value to "".
     * @param fileName One csv file's file name.
     * @param header A list of this csv file's headers.
     * @param combinedHeaders The list of the combined/target csv file's headers.
     */
    public void writeCombinedCsv(String fileName, List<String> header, List<String> combinedHeaders) {
        File file = new File(fileName);
        String line = null;
        String[] cols = null;
        int n = combinedHeaders.size();
        String name = "\"" + fileName + "\"";
        String nul = "\"\"";

        // find columns' map from combinedHeaders to this file's header
        Map<String, Integer> headerIndices = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            headerIndices.put(header.get(i), i);
        }

        int[] map = new int[n];
        for (int i = 0; i < n; i++) {
            if (headerIndices.containsKey(combinedHeaders.get(i))) {
                map[i] = headerIndices.get(combinedHeaders.get(i));
            }
            else {
                map[i] = -1;
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            line = br.readLine(); // skip the header line
            while ((line = br.readLine()) != null) {
                cols = line.split(",");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n - 1; i++) {
                    if (map[i] != -1) {
                        sb.append(cols[map[i]]);
                    }
                    else {
                        sb.append(nul);
                    }
                    sb.append(",");
                }
                sb.append(name);
                System.out.println();
                System.out.print(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}