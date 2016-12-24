package intoxicant.analytics.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.Pair;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;
import org.junit.Before;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * User: jconwell
 * Unit tests for StopwordAnnotator
 */
public class StopwordAnnotatorTest {

    /**
     * The standard Lucene stopword list is as follows:
             "a", "an", "and", "are", "as", "at", "be", "but", "by",
             "for", "if", "in", "into", "is", "it",
             "no", "not", "of", "on", "or", "such",
             "that", "the", "their", "then", "there", "these",
             "they", "this", "to", "was", "will", "with"
     */

    //sample text for tests
    private static final String example = "The history of NLP generally starts in the 1950s, although work can be found from earlier periods.";

    //adding a couple extra terms to standard lucene list to test against
    private static final String customStopWordList = "start,starts,period,periods,a,an,and,are,as,at,be,but,by,for,if,in,into,is,it,no,not,of,on,or,such,that,the,their,then,there,these,they,this,to,was,will,with";

    Properties props = new Properties();

    @Before
    public void before() {
        props.put("annotators", "tokenize, ssplit, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
    }

    @org.junit.Test
    public void testRequirementsSatisfied() throws Exception {
        StopwordAnnotator sw = new StopwordAnnotator(StopwordAnnotator.ANNOTATOR_CLASS , props);
        assertTrue(sw.requirementsSatisfied().size() == 1);
        assertTrue(sw.requirementsSatisfied().contains(StopwordAnnotator.STOPWORD_REQUIREMENT));
    }

    @org.junit.Test
    public void testRequires() throws Exception {

        //Test that if lemmatization is not being checked, requirements only returns tokenize and ssplit
        StopwordAnnotator sw = new StopwordAnnotator(StopwordAnnotator.ANNOTATOR_CLASS, props);
        assertTrue(sw.requires().size() == 2);
        assertTrue(sw.requires().contains(StopwordAnnotator.TOKENIZE_REQUIREMENT));
        assertTrue(sw.requires().contains(StopwordAnnotator.SSPLIT_REQUIREMENT));

        //Test that is lemmatization is being checked, requirements returns tokenize, ssplit, pos, and lemms
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
        props.setProperty(StopwordAnnotator.CHECK_LEMMA, "true");
        sw = new StopwordAnnotator(StopwordAnnotator.ANNOTATOR_CLASS, props);
        assertTrue(sw.requires().size() == 4);
        assertTrue(sw.requires().contains(StopwordAnnotator.TOKENIZE_REQUIREMENT));
        assertTrue(sw.requires().contains(StopwordAnnotator.SSPLIT_REQUIREMENT));
        assertTrue(sw.requires().contains(StopwordAnnotator.POS_REQUIREMENT));
        assertTrue(sw.requires().contains(StopwordAnnotator.LEMMA_REQUIREMENT));
    }

    @org.junit.Test
    public void testGetType() throws Exception {
        StopwordAnnotator sw = new StopwordAnnotator(StopwordAnnotator.ANNOTATOR_CLASS, props);
        assertTrue(sw.getType().equals(Pair.makePair(true, true).getClass()));
    }


    /**
     * *****The following unit tests demonstrate how to use the StopwordAnnotator in CoreNlp*****
     */


    /**
     * Test to validate that stopwords are properly annotated in the token list
     * @throws Exception
     */
    @org.junit.Test
    public void testLuceneStopwordList() throws Exception {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(example);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        //get the standard lucene stopword set
        Set<?> stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

        for (CoreLabel token : tokens) {

            //get the stopword annotation
            Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);

            String word = token.word().toLowerCase();
            if (stopWords.contains(word)) {
                assertTrue(stopword.first());
            }
            else {
                assertFalse(stopword.first());
            }

            //not checking lemma, so always false
            assertFalse(stopword.second());
        }
    }

    /**
     * Test to validate that the custom stopword list words
     * @throws Exception
     */
    @org.junit.Test
    public void testCustomStopwordList() throws Exception {

        //setup coreNlp properties for stopwords. Note the custom stopword list property
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
        props.setProperty(StopwordAnnotator.STOPWORDS_LIST, customStopWordList);

        //get the custom stopword set
        Set<?> stopWords = StopwordAnnotator.getStopWordList(Version.LUCENE_36, customStopWordList, true);

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(example);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens) {

            //get the stopword annotation
            Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);

            String word = token.word().toLowerCase();
            if (stopWords.contains(word)) {
                assertTrue(stopword.first());
            }
            else {
                assertFalse(stopword.first());
            }

            //not checking lemma, so always false
            assertFalse(stopword.second());
        }
    }

    /**
     * Test to validate that lemma values are checked against the (custom) stopword list
     *
     * NOTE: since we're loading the pos model into memory you'll need to set the VM memory size via '-Xms512m -Xmx1048m'
     * @throws Exception
     */
    @org.junit.Test
    public void testStopwordsWithLemma() throws Exception {

        //setup coreNlp properties for stopwords. Note the custom stopword list and check for lemma property
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
        props.setProperty(StopwordAnnotator.STOPWORDS_LIST, customStopWordList);
        props.setProperty(StopwordAnnotator.CHECK_LEMMA, "true");

        //get the custom stopword set
        Set<?> stopWords = StopwordAnnotator.getStopWordList(Version.LUCENE_36, customStopWordList, true);

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(example);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens) {

            //get the stopword annotation
            Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);

            String word = token.word().toLowerCase();
            if (stopWords.contains(word)) {
                assertTrue(stopword.first());
            }
            else {
                assertFalse(stopword.first());
            }

            String lemma = token.lemma().toLowerCase();
            if (stopWords.contains(lemma)) {
                assertTrue(stopword.first());
            }
            else {
                assertFalse(stopword.first());
            }
        }
    }
}
