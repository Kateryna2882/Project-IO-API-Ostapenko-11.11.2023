import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BookAnalyzer {
    private static final String FILE_EXTENSION = ".txt";
    private static final String DIRECTORY_NAME = "src";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the title of the book to analyze: ");
        String bookTitle = scanner.nextLine();

        Path harryPotter = Paths.get(DIRECTORY_NAME, bookTitle + FILE_EXTENSION);
        if (!isValidBook(harryPotter)) {
            System.out.println("The book '"
                    + bookTitle + "' is missing in the src/ directory or it is not a file.");
            return;
        }

        try (InputStream fileInput = Files.newInputStream(harryPotter);
             InputStreamReader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8)) {
            BufferedReader bookReader = new BufferedReader(reader);

            String content = bookReader.lines().collect(Collectors.joining(System.lineSeparator()));
            Map<String, Integer> wordStatistics = analyzeText(content);
            List<String> topWords = getTopWords(wordStatistics, 10);

            writeAndPrintStatistics(bookTitle, wordStatistics, topWords);
        } catch (FileNotFoundException e) {
            System.out.println("The file not found : " + harryPotter);

        } catch (IOException e) {
            System.out.println("Error processing file '" + harryPotter + "': " + e.getMessage());
//            e.printStackTrace(); // maybe add ?
        }
    }

    private static boolean isValidBook(Path filePath) {
        return Files.isRegularFile(filePath) && !Files.isDirectory(filePath);
    }

    private static Map<String, Integer> analyzeText(String text) {
        Map<String, Integer> wordStatistics = new HashMap<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (word.length() > 2) {
                wordStatistics.put(word, wordStatistics.getOrDefault(word, 0) + 1);
            }
        }

        return wordStatistics;
    }

    private static List<String> getTopWords(Map<String, Integer> wordStatistics, int topCount) {
        return wordStatistics.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static void writeAndPrintStatistics(String bookTitle, Map<String, Integer> wordStatistics,
                                                List<String> topWords) {
        String fileName = bookTitle + "_statistic.txt";

        try (OutputStream outputStream = new FileOutputStream(fileName);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            wordStatistics.forEach((word, count) -> {
                try {
                    bufferedWriter.write(word + " -> " + count + "\n");
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                }
            });

            bufferedWriter.write("Total words: " + wordStatistics.size() + "\n");

            System.out.println("Statistics:");

            topWords.forEach(word -> System.out.println(word + " -> " + wordStatistics.get(word)));

            System.out.println("Total unique words: " + wordStatistics.size());

        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
