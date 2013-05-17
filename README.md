coreNlp
=======

This library is a growing set of extensions for and tools to work with [CoreNlp](http://www-nlp.stanford.edu/software/corenlp.shtml).

Identifying Stopwords in CoreNlp
--------------------------------

CoreNlp doesnt have stopword identification built in, so I wrote an extension to its analytics pipeline (called Annatators) to check if a token's word and lemma value are stopwords.

By default, the StopwordAnnotator uses the built in Lucene stopword list, but you have to option to pass in a custom list of stopwords for it to use instead.  You can also specify if the StopwordAnnotator should check the lemma of the token against the stopword list or not.

For examples of how to use the StopwordAnnotator, takea look at StopwordAnnotatorTest.java 

Friendly API for building a new StanfordCoreNLP instance
--------------------------------------------------------

There is also a friendly api for configuring the analyzers you want to use when you create a new StanfordCoreNLP instance.  This beats building up a properly formatted string (in the correct order) of the list of analyzers CoreNlp will load.  There are also a set of static factory functions built around many common combinations of options.

Again, check out the unit tests for examples on how to use them.

NOTE: the unit tests actually create an instance of StanfordCoreNLP, so if you want to build the jar with maven you'll need to configure maven to have a larger heap size as several models require a fair bit of memory
