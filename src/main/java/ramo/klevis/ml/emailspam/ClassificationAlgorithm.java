package ramo.klevis.ml.emailspam;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm;
import org.apache.spark.mllib.regression.GeneralizedLinearModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by klevis.ramo on 9/26/2017.
 */
public class ClassificationAlgorithm implements Serializable {

    private final PrepareData prepareData;
    private int featureSIze = 12000;
    private GeneralizedLinearAlgorithm model;
    private Map<String, Integer> vocabulary;
    private GeneralizedLinearModel linearModel;
    private transient JavaSparkContext sparkContext;

    public ClassificationAlgorithm(int featureSIze, GeneralizedLinearAlgorithm model) {
        this.featureSIze = featureSIze;
        prepareData = new PrepareData(featureSIze);
        this.model = model;
    }

    public double test(String emailString) throws IOException {
        Email email = prepareData.prepareEmailForTesting(emailString);
        return linearModel.predict(transformToFeatureVector(email, vocabulary));
    }

    public boolean isTrained() {
        return linearModel != null;
    }

    public void dispose() {
        sparkContext.close();
    }

    private List<LabeledPoint> convertToLabelPoints() throws Exception {
        ArrayList<Email> emails = new ArrayList<>();
        emails.addAll(prepareData.filesToEmailWords("allInOneSpamBase/spam"));
        emails.addAll(prepareData.filesToEmailWords("allInOneSpamBase/hard_ham"));
        emails.addAll(prepareData.filesToEmailWords("allInOneSpamBase/easy_ham"));
        emails.addAll(prepareData.filesToEmailWords("allInOneSpamBase/easy_ham_2"));
        emails.addAll(prepareData.filesToEmailWords("allInOneSpamBase/spam_2"));
        return emails.stream().parallel().map(e -> new LabeledPoint(e.isSpam() == true ? 1 : 0, transformToFeatureVector(e, vocabulary))).collect(Collectors.toList());
    }

    public MulticlassMetrics execute() throws Exception {
        vocabulary = prepareData.createVocabulary();
        List<LabeledPoint> labeledPoints = convertToLabelPoints();
        sparkContext = createSparkContext();
        JavaRDD<LabeledPoint> labeledPointJavaRDD = sparkContext.parallelize(labeledPoints);
        JavaRDD<LabeledPoint>[] splits = labeledPointJavaRDD.randomSplit(new double[]{0.6, 0.4}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> test = splits[1];


        linearModel = model.run(training.rdd());

        JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(
                (Function<LabeledPoint, Tuple2<Object, Object>>) p -> {
                    Double prediction = linearModel.predict(p.features());
                    return new Tuple2<>(prediction, p.label());
                }
        );

        return new MulticlassMetrics(predictionAndLabels.rdd());
    }

    private Vector transformToFeatureVector(Email email, Map<String, Integer> vocabulary) {
        List<String> words = email.getWords();
        HashMap<String, Integer> countWords = prepareData.countWords(words);
        double[] features = new double[featureSIze];
        for (Map.Entry<String, Integer> word : countWords.entrySet()) {
            Integer index = vocabulary.get(word.getKey());
            if (index != null) {
                features[index] = word.getValue();
            }
        }
        return Vectors.dense(features);
    }

    private JavaSparkContext createSparkContext() {
        SparkConf conf = new SparkConf().setAppName("Finance Fraud Detection").setMaster("local[*]");
        return new JavaSparkContext(conf);
    }

}
