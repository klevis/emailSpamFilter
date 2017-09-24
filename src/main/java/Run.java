import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.catalyst.expressions.In;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by klevis.ramo on 9/22/2017.
 */
public class Run {

    public static void main(String[] args) throws IOException {

        Map<String, Integer> vocabulary = createVocabulary();
//        ArrayList<String> all = new ArrayList<>();
//        all.addAll(filesToWords("allInOneSpamBase/easy_ham"));
//        all.addAll(filesToWords("allInOneSpamBase/easy_ham_2"));
//        all.addAll(filesToWords("allInOneSpamBase/hard_ham"));
//        all.addAll(filesToWords("allInOneSpamBase/spam"));
//        all.addAll(filesToWords("allInOneSpamBase/spam_2"));

        System.out.println(vocabulary.size());
    }

    private static Map<String, Integer> createVocabulary() throws IOException {
        String first = "allInOneSpamBase/spam";
        String second = "allInOneSpamBase/spam_2";
        List<String> collect1 = filesToWords(first);
        List<String> collect2 = filesToWords(second);

        System.out.println(collect1.size() + collect2.size());
        HashMap<String, Integer> countWords = new HashMap<>();
        ArrayList<String> all = new ArrayList<>(collect1);
        all.addAll(collect2);
        for (String s : all) {
            if (countWords.get(s) == null) {
                countWords.put(s, 1);
            } else {
                countWords.put(s, countWords.get(s) + 1);
            }
        }
        System.out.println();

        List<Map.Entry<String, Integer>> sortedVocabulary = countWords.entrySet().stream().parallel().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).collect(Collectors.toList());
        final int[] index = {0};
        return sortedVocabulary.stream().limit(50000).collect(Collectors.toMap(e -> e.getKey(), e -> index[0]++));
    }

    private static List<String> filesToWords(String first) throws IOException {
        List<String> collect = Files.walk(Paths.get(first)).parallel()
                .filter(Files::isRegularFile)
                .flatMap(file -> {
                    try {

                        return Stream.of(new String(Files.readAllBytes(file)).toLowerCase());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList());

        System.out.println("collect = " + collect.size());
        return collect.stream().parallel().flatMap(e -> tokenizeIntoWords(preapreEmail(e)).stream()).collect(Collectors.toList());
    }

    private JavaSparkContext createSparkContext() {
        SparkConf conf = new SparkConf().setAppName("Finance Fraud Detection").setMaster("local[*]");
        return new JavaSparkContext(conf);
    }

    private static List<String> tokenizeIntoWords(String dollarReplaced) {
        String delim = "[' @$/#.-:&*+=[]?!(){},''\\\">_<;%'\t\n\r\f";
        StringTokenizer stringTokenizer = new StringTokenizer(dollarReplaced, delim);
        List<String> wordsList = new ArrayList<>();
        while (stringTokenizer.hasMoreElements()) {
            String word = (String) stringTokenizer.nextElement();
            String nonAlphaNumericRemoved = word.replaceAll("[^a-zA-Z0-9]", "");
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(nonAlphaNumericRemoved);
            stemmer.stem();
            String stemmed = stemmer.getCurrent();
            wordsList.add(stemmed);
        }
        return wordsList;
    }

    private static String preapreEmail(String email) {
        String withoutHeader = email.substring(email.indexOf("\n\n"), email.length());
        String tagsRemoved = withoutHeader.replaceAll("<[^<>]+>", "");
        String numberedReplaced = tagsRemoved.replaceAll("[0-9]+", "XNUMBERX ");
        String urlReplaced = numberedReplaced.replaceAll("(http|https)://[^\\s]*", "XURLX ");
        String emailReplaced = urlReplaced.replaceAll("[^\\s]+@[^\\s]+", "XEMAILX ");
        String dollarReplaced = emailReplaced.replaceAll("[$]+", "XMONEYX ");
        return dollarReplaced;
    }


}
