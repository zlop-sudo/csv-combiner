import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CsvCombinerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void main() throws IOException {
        String expected = new String(Files.readAllBytes(Paths.get("src/main/data/result.csv")));
        String[] inputFiles = new String[] {"src/main/data/accessories.csv", "src/main/data/clothing.csv"};
        CsvCombiner.main(inputFiles);
        assertEquals(expected, outContent.toString());
    }
}