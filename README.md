coreNlp
=======

Extensions for and tools to work with CoreNlp

StopwordAnnotator is a CoreNlp Annotator that will mark tokens as stopwords.  You can use the Lucene default list
of stopwords, or you can provide a custom list of stopwords.  You can also specify if the stopword checker should
check the lemma of the token against the stopword list.

Look at StopwordAnnotatorTest.java for examples of how to use StopwordAnnotator