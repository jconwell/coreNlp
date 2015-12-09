package intoxicant.analytics.corenlp;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.Pair;

/**
 * User: jconwell
 * CoreNlp Annotator that checks if in coming token is a stopword
 */
public class StopwordAnnotator implements Annotator, CoreAnnotation<Pair<Boolean, Boolean>> {

    /**
     * stopword annotator class name used in annotators property
     */
    public static final String ANNOTATOR_CLASS = "stopword";

    public static final String STANFORD_STOPWORD = ANNOTATOR_CLASS;
    public static final Requirement STOPWORD_REQUIREMENT = new Requirement(STANFORD_STOPWORD);

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

    public StopwordAnnotator() {
	this(new Properties());
    }

    public StopwordAnnotator(String notUsed, Properties props) {
	this(props);
    }
    public StopwordAnnotator(Properties props) {
        this.props = props;

        this.checkLemma = Boolean.parseBoolean(props.getProperty(CHECK_LEMMA, "false"));

        if (this.props.containsKey(STOPWORDS_LIST)) {
            String stopwordList = props.getProperty(STOPWORDS_LIST);
            boolean ignoreCase = Boolean.parseBoolean(props.getProperty(IGNORE_STOPWORD_CASE, "false"));
            this.stopwords = getStopWordList(Version.LUCENE_36, stopwordList, ignoreCase);
        } else {
            this.stopwords = (CharArraySet) StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        }
    }

    @Override
    public void annotate(Annotation annotation) {
        if (stopwords != null && stopwords.size() > 0 && annotation.containsKey(TokensAnnotation.class)) {
            List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                boolean isWordStopword = stopwords.contains(token.word().toLowerCase());
                boolean isLemmaStopword = checkLemma ? stopwords.contains(token.word().toLowerCase()) : false;
                Pair<Boolean, Boolean> pair = Pair.makePair(isWordStopword, isLemmaStopword);
                token.set(StopwordAnnotator.class, pair);
            }
        }
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(STOPWORD_REQUIREMENT);
    }

    @Override
    public Set<Requirement> requires() {
        if (checkLemma) {
            return TOKENIZE_SSPLIT_POS_LEMMA;
        }
        else {
            return TOKENIZE_AND_SSPLIT;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Pair<Boolean, Boolean>> getType() {
        return (Class<Pair<Boolean, Boolean>>) boolPair;
    }

    public static CharArraySet getStopWordList(Version luceneVersion, String stopwordList, boolean ignoreCase) {
        String[] terms = stopwordList.split(",");
        CharArraySet stopwordSet = new CharArraySet(luceneVersion, terms.length, ignoreCase);
        for (String term : terms) {
            stopwordSet.add(term);
        }
        return CharArraySet.unmodifiableSet(stopwordSet);
    }
}
