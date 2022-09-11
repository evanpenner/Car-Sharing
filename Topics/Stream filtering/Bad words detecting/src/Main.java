import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.*;

class BadWordsDetector {

    private static Stream<String> createBadWordsDetectingStream(String text, 
                                                                List<String> badWords) {
        // write your code here
        return Stream.of();
    }

    /* Do not change the code below */
    public static void main(String[] args) {
        Stream.iterate(1, i -> i + 2)
                .skip(5)
                .limit(5)
                .forEach(n -> System.out.print(n + " "));
        Scanner scanner = new Scanner(System.in);
        String[] parts = scanner.nextLine().split(";");

        // the first part is a text
        String text = parts[0];

        // the second part is a bad words dictionary
        List<String> dict = parts.length > 1 ?
                Arrays.asList(parts[1].split(" ")) :
                Collections.singletonList("");

        System.out.println(createBadWordsDetectingStream(text, dict).collect(Collectors.toList()));
    }

}