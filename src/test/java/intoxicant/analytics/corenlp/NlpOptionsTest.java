package intoxicant.analytics.coreNlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: jconwell
 * Date: 5/17/13
 * Unit tests for NlpOptions class
 *
 * NOTE: since we're loading the pos model into memory you'll need to set the VM memory size via '-Xms512m -Xmx1048m'
 */
public class NlpOptionsTest {
    private static final int maxSentenceDist = 125;

    @Test
    public void tokenizationOnlyTest() {
        NlpOptions options = NlpOptions.tokenizationOnly(false);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos"));
        assertFalse(options.lemmatisation);
        assertFalse(options.namedEntityRecognition);
        assertFalse(options.namedEntityRecognitionRegex);
        assertFalse(options.sentenceParser);
        assertFalse(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == -1);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }

    @Test
    public void tokenizationOnlyWithLemmaTest() {
        NlpOptions options = NlpOptions.tokenizationOnly(true);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos, lemma"));
        assertTrue(options.lemmatisation);
        assertFalse(options.namedEntityRecognition);
        assertFalse(options.namedEntityRecognitionRegex);
        assertFalse(options.sentenceParser);
        assertFalse(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == -1);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }

    @Test
    public void namedEntityRecognitionTest() {
        NlpOptions options = NlpOptions.namedEntityRecognition(false, false);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos, lemma, ner"));
        assertTrue(options.lemmatisation);
        assertTrue(options.namedEntityRecognition);
        assertFalse(options.namedEntityRecognitionRegex);
        assertFalse(options.sentenceParser);
        assertFalse(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == -1);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }

    @Test
    public void namedEntityRecognitionWithRegexTest() {
        NlpOptions options = NlpOptions.namedEntityRecognition(true, false);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos, lemma, ner, regexner"));
        assertTrue(options.lemmatisation);
        assertTrue(options.namedEntityRecognition);
        assertTrue(options.namedEntityRecognitionRegex);
        assertFalse(options.sentenceParser);
        assertFalse(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == -1);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }

    @Test
    public void namedEntitiesWithCoreferenceAnalysisTest() {
        NlpOptions options = NlpOptions.namedEntitiesWithCoreferenceAnalysis(true, maxSentenceDist, false);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref"));
        assertTrue(options.lemmatisation);
        assertTrue(options.namedEntityRecognition);
        assertTrue(options.namedEntityRecognitionRegex);
        assertTrue(options.sentenceParser);
        assertTrue(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == maxSentenceDist);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }

    @Test
    public void sentenceParsingTest() {
        NlpOptions options = NlpOptions.sentenceParser(true);
        Properties props = options.getNlpProperties();
        assertTrue(props.getProperty("annotators").equals("tokenize, ssplit, pos, lemma, parse"));
        assertTrue(options.lemmatisation);
        assertFalse(options.namedEntityRecognition);
        assertFalse(options.namedEntityRecognitionRegex);
        assertTrue(options.sentenceParser);
        assertFalse(options.coreferenceAnalysis);
        assertTrue(options.corefMaxSentenceDist == -1);
        assertFalse(options.corefPostProcessing);

        StanfordCoreNLP nlp = options.buildNlpAnalyzer();
        assertNotNull(nlp);
    }
}
