package com.cranfield;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class Components {
    public static Analyzer analyzer(String name) {
        switch (name) {
            case "standard":
                return new StandardAnalyzer(CharArraySet.EMPTY_SET);
            case "standard-stop":
                return new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
            case "english":
                return new EnglishAnalyzer();
            case "kstem":
                return new Analyzer() {
                    @Override
                    protected TokenStreamComponents createComponents(String fieldName) {
                        Tokenizer source = new StandardTokenizer();
                        TokenStream ts = new LowerCaseFilter(source);
                        ts = new StopFilter(ts, EnglishAnalyzer.getDefaultStopSet());
                        ts = new KStemFilter(ts);
                        return new TokenStreamComponents(source, ts);
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown analyzer: " + name);
        }
    }

    public static Similarity similarity(String spec) {
        String[] p = spec.split(":");
        switch (p[0]) {
            case "classic":
                return new ClassicSimilarity(); // TF-IDF / Vector Space Model
            case "bm25":
                return p.length == 3
                        ? new BM25Similarity(Float.parseFloat(p[1]), Float.parseFloat(p[2]))
                        : new BM25Similarity();
            case "lmd":
                return p.length > 1
                        ? new LMDirichletSimilarity(Float.parseFloat(p[1]))
                        : new LMDirichletSimilarity();
            case "lmjm":
                return new LMJelinekMercerSimilarity(p.length > 1 ? Float.parseFloat(p[1]) : 0.7f);
            default:
                throw new IllegalArgumentException("Unknown similarity: " + spec);
        }
    }
}
