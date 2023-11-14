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
            BufferedReader bufferedReader = new BufferedReader(reader);

            String content = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            Map<String, Integer> wordFrequencyMap = analyzeText(content);
            List<String> topWords = getTopWords(wordFrequencyMap, 10);

            writeAndPrintStatistics(bookTitle, wordFrequencyMap, topWords);

        } catch (IOException e) {
            System.out.println("Error processing file '" + harryPotter + "': " + e.getMessage());
        }
    }

    private static boolean isValidBook(Path filePath) {
        return Files.isRegularFile(filePath) && !Files.isDirectory(filePath);
    }

    private static Map<String, Integer> analyzeText(String text) {
        Map<String, Integer> wordFrequencyMap = new HashMap<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (word.length() > 2) {
                wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
            }
        }

        return wordFrequencyMap;
    }

    private static List<String> getTopWords(Map<String, Integer> wordFrequencyMap, int topCount) {
        return wordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static void writeAndPrintStatistics(String bookTitle, Map<String, Integer> wordFrequencyMap,
                                                List<String> topWords) {
        String fileName = bookTitle + "_statistic.txt";

        try (OutputStream outputStream = new FileOutputStream(fileName);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            wordFrequencyMap.forEach((word, count) -> {
                try {
                    bufferedWriter.write(word + " -> " + count + "\n");
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                }
            });

            bufferedWriter.write("Total words: " + wordFrequencyMap.size() + "\n");

            System.out.println("Statistics:");

            topWords.forEach(word -> System.out.println(word + " -> " + wordFrequencyMap.get(word)));

            System.out.println("Total unique words: " + wordFrequencyMap.size());

        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
