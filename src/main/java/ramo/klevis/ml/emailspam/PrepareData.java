package ramo.klevis.ml.emailspam;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by klevis.ramo on 10/9/2017.
 */
public class PrepareData {

    private final long featureSIze;

    public PrepareData(long featureSIze) {
        this.featureSIze = featureSIze;
    }


    public Email prepareEmailForTesting(String emailString) {
        return new Email(tokenizeIntoWords(preapreEmail(emailString.toLowerCase())));
    }

    public Map<String, Integer> createVocabulary() throws Exception {
        String first = "allInOneSpamBase/spam";
        String second = "allInOneSpamBase/spam_2";
        List<String> collect1 = filesToWords(first);
        List<String> collect2 = filesToWords(second);

        System.out.println(collect1.size() + collect2.size());
        ArrayList<String> all = new ArrayList<>(collect1);
        all.addAll(collect2);
        HashMap<String, Integer> countWords = countWords(all);

        List<Map.Entry<String, Integer>> sortedVocabulary = countWords.entrySet().stream().parallel().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).collect(Collectors.toList());
        final int[] index = {0};
        return sortedVocabulary.stream().limit(featureSIze).collect(Collectors.toMap(e -> e.getKey(), e -> index[0]++));
    }

    public List<Email> filesToEmailWords(String fileName) throws IOException, URISyntaxException {
        URI uri = this.getClass().getResource("/" + fileName).toURI();
        Path start = getPath(uri);
        List<Email> collect = Files.walk(start).parallel()
                .filter(Files::isRegularFile)
                .map(file -> {
                    try {

                        return new Email(tokenizeIntoWords(preapreEmail(new String(Files.readAllBytes(file)).toLowerCase())), fileName.contains("spam"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList());

        System.out.println("collect = " + collect.size() + " " + fileName);
        return collect;
    }

    HashMap<String, Integer> countWords(List<String> all) {
        HashMap<String, Integer> countWords = new HashMap<>();
        for (String s : all) {
            if (countWords.get(s) == null) {
                countWords.put(s, 1);
            } else {
                countWords.put(s, countWords.get(s) + 1);
            }
        }
        return countWords;
    }

    private Path getPath(URI uri) throws IOException {
        Path start = null;
        try {
            start = Paths.get(uri);
        } catch (FileSystemNotFoundException e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            FileSystems.newFileSystem(uri, env);
            start = Paths.get(uri);
        }
        return start;
    }

    private List<String> filesToWords(String fileName) throws Exception {
        URI uri = this.getClass().getResource("/" + fileName).toURI();
        Path start = getPath(uri);
        List<String> collect = Files.walk(start).parallel()
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

    private List<String> tokenizeIntoWords(String dollarReplaced) {
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

    private String preapreEmail(String email) {
        int beginIndex = email.indexOf("\n\n");
        String withoutHeader = email;
        if (beginIndex > 0) {
            withoutHeader = email.substring(beginIndex, email.length());
        }
        String tagsRemoved = withoutHeader.replaceAll("<[^<>]+>", "");
        String numberedReplaced = tagsRemoved.replaceAll("[0-9]+", "XNUMBERX ");
        String urlReplaced = numberedReplaced.replaceAll("(http|https)://[^\\s]*", "XURLX ");
        String emailReplaced = urlReplaced.replaceAll("[^\\s]+@[^\\s]+", "XEMAILX ");
        String dollarReplaced = emailReplaced.replaceAll("[$]+", "XMONEYX ");
        return dollarReplaced;
    }
}
