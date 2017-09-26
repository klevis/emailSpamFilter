package ramo.klevis.ml.emailspam;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.tartarus.snowball.ext.PorterStemmer;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
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
 * Created by klevis.ramo on 9/26/2017.
 */
public class LogisticRegression implements Serializable {

    private int featureSIze = 12000;
    private LogisticRegressionModel model;
    private Map<String, Integer> vocabulary;

    public LogisticRegression(int featureSIze) {
        this.featureSIze = featureSIze;
    }

    private static JavaSparkContext createSparkContext() {
        SparkConf conf = new SparkConf().setAppName("Finance Fraud Detection").setMaster("local[*]");
        return new JavaSparkContext(conf);
    }

    private Map<String, Integer> createVocabulary() throws Exception {
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

    public double test(String emailString) throws IOException {
        Email email = new Email(tokenizeIntoWords(preapreEmail(emailString.toLowerCase())));
        return model.predict(transformToFeatureVector(email, vocabulary));
    }

    public boolean isTrained() {
        return model != null;
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


    private List<Email> filesToEmailWords(String fileName) throws IOException, URISyntaxException {
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

    private List<LabeledPoint> convertToLabelPoints(Map<String, Integer> vocabulary) throws Exception {
        ArrayList<Email> emails = new ArrayList<>();
        emails.addAll(filesToEmailWords("allInOneSpamBase/spam"));
        emails.addAll(filesToEmailWords("allInOneSpamBase/hard_ham"));
        emails.addAll(filesToEmailWords("allInOneSpamBase/easy_ham"));
        emails.addAll(filesToEmailWords("allInOneSpamBase/easy_ham_2"));
        emails.addAll(filesToEmailWords("allInOneSpamBase/spam_2"));
        return emails.stream().parallel().map(e -> new LabeledPoint(e.isSpam() == true ? 1 : 0, transformToFeatureVector(e, vocabulary))).collect(Collectors.toList());
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

    public MulticlassMetrics execute() throws Exception {
        vocabulary = createVocabulary();
        List<LabeledPoint> labeledPoints = convertToLabelPoints(vocabulary);
        JavaSparkContext sparkContext = createSparkContext();
        JavaRDD<LabeledPoint> labeledPointJavaRDD = sparkContext.parallelize(labeledPoints);
        JavaRDD<LabeledPoint>[] splits = labeledPointJavaRDD.randomSplit(new double[]{0.6, 0.4}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> test = splits[1];

        model = new LogisticRegressionWithLBFGS()
                .setNumClasses(2)
                .run(training.rdd());

        JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(
                (Function<LabeledPoint, Tuple2<Object, Object>>) p -> {
                    Double prediction = model.predict(p.features());
                    return new Tuple2<>(prediction, p.label());
                }
        );

        return new MulticlassMetrics(predictionAndLabels.rdd());
    }

    private Vector transformToFeatureVector(Email email, Map<String, Integer> vocabulary) {
        List<String> words = email.getWords();
        HashMap<String, Integer> countWords = countWords(words);
        double[] features = new double[featureSIze];
        for (Map.Entry<String, Integer> word : countWords.entrySet()) {
            Integer index = vocabulary.get(word.getKey());
            if (index != null) {
                features[index] = word.getValue();
            }
        }
        return Vectors.dense(features);
    }

    private HashMap<String, Integer> countWords(List<String> all) {
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
}
