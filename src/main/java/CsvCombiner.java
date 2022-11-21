import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class is a command line based program, the main entrance of the csv combiner.
 * @author Qiaodan Zhao
 * @version 1.0
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
                throw new IllegalArgumentException("Inputfile must be csv format!");
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
        List<List<String>> hearders = new ArrayList<List<String>>();

        for (String inputFileName : inputFileNames) {
            File file = new File(inputFileName);
            String line = null;
            String[] firstLine = null;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                if ((line = br.readLine()) != null) {
                    firstLine = line.split(",");
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String> header = new ArrayList<String>();
            for (String col : firstLine) {
                header.add(col);
            }
            hearders.add(header);
        }

        return hearders;
    }

    /**
     * This method merges all csv files' headers into one in order.
     * @param hearders List of every csv file's headers.
     * @return List of combined csv file's headers.
     */
    public List<String> combineHeaders(List<List<String>> hearders) {
        List<String> combinedHeaders = new ArrayList<String>();
        Set<String> set = new HashSet<String>();

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
     * @param combinedHeaders
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
        int[] map = new int[n];
        for (int i = 0; i < n; i++) {
            map[i] = -1;
            for (int j = 0; j < header.size(); j++) {
                if (combinedHeaders.get(i).equals(header.get(j))) {
                    map[i] = j;
                    break;
                }
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                cols = line.split(",");
                StringBuffer sb = new StringBuffer();
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
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}