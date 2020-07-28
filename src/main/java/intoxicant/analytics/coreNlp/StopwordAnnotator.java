package intoxicant.analytics.coreNlp;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.Pair;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

import java.util.*;

/**
 * User: jconwell
 * CoreNlp Annotator that checks if in coming token is a stopword
 */
public class StopwordAnnotator implements Annotator, CoreAnnotation<Pair<Boolean, Boolean>> {

    /**
     * stopword annotator class name used in annotators property
     */
    public static final String ANNOTATOR_CLASS = "stopword";

    /**
     * Property key to specify the comma delimited list of custom stopwords
     */
    public static final String STOPWORDS_LIST = "stopword-list";

    /**
     * Property key to specify if stopword list is case insensitive
     */
    public static final String IGNORE_STOPWORD_CASE = "ignore-stopword-case";

    /**
     * Property key to specify of StopwordAnnotator should check word lemma as stopword
     */
    public static final String CHECK_LEMMA = "check-lemma";

    private static Class<? extends Pair> boolPair = Pair.makePair(true, true).getClass();

    private Properties props;
    private CharArraySet stopwords;
    private boolean checkLemma;

    public StopwordAnnotator(String annotatorClass, Properties props) {
        this.props = props;

        this.checkLemma = Boolean.parseBoolean(props.getProperty(CHECK_LEMMA, "false"));
        boolean ignoreCase = Boolean.parseBoolean(props.getProperty(IGNORE_STOPWORD_CASE, "false"));

        if (this.props.containsKey(STOPWORDS_LIST)) {
            String stopwordList = props.getProperty(STOPWORDS_LIST);
            this.stopwords = getStopWordList(Version.LUCENE_36, stopwordList, ignoreCase);
        } else {
            this.stopwords = new CharArraySet(Version.LUCENE_36, StopAnalyzer.ENGLISH_STOP_WORDS_SET, ignoreCase);
        }
    }

    @Override
    public void annotate(Annotation annotation) {
        if (stopwords != null && stopwords.size() > 0 && annotation.containsKey(TokensAnnotation.class)) {
            List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                boolean isWordStopword = stopwords.contains(token.word());
                boolean isLemmaStopword = checkLemma && stopwords.contains(token.word());
                Pair<Boolean, Boolean> pair = Pair.makePair(isWordStopword, isLemmaStopword);
                token.set(StopwordAnnotator.class, pair);
            }
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(StopwordAnnotator.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        Set<Class<? extends CoreAnnotation>> requeredAnnotations = new HashSet<>();
        requeredAnnotations.add(CoreAnnotations.TextAnnotation.class);
        requeredAnnotations.add(CoreAnnotations.TokensAnnotation.class);
        if (checkLemma) {
            requeredAnnotations.add(CoreAnnotations.LemmaAnnotation.class);
            requeredAnnotations.add(CoreAnnotations.PartOfSpeechAnnotation.class);
        }
        return requeredAnnotations;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Pair<Boolean, Boolean>> getType() {
        return (Class<Pair<Boolean, Boolean>>) boolPair;
    }

    public static CharArraySet getStopWordList(Version luceneVersion, String stopwordList, boolean ignoreCase) {
        String[] terms = stopwordList.split(",");
        CharArraySet stopwordSet = new CharArraySet(luceneVersion, terms.length, ignoreCase);
        Collections.addAll(stopwordSet, terms);
        return CharArraySet.unmodifiableSet(stopwordSet);
    }
}
