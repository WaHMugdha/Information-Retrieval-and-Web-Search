package com.cranfield;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Searcher implements Closeable {

    private final Directory dir;
    private final DirectoryReader reader;
    private final IndexSearcher searcher;
    private final QueryParser parser;

    public Searcher(Path indexDir, Analyzer analyzer, Similarity similarity, float titleBoost) throws IOException {
        dir = FSDirectory.open(indexDir);
        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        Map<String, Float> boosts = new HashMap<>();
        boosts.put("title", titleBoost);
        boosts.put("content", 1.0f);
        parser = new MultiFieldQueryParser(new String[] {"title", "content"}, analyzer, boosts);
    }

    public ScoreDoc[] search(String text, int k) throws ParseException, IOException {
        Query q = parser.parse(QueryParser.escape(text.toLowerCase()));
        return searcher.search(q, k).scoreDocs;
    }

    public Document doc(int luceneDocId) throws IOException {
        return searcher.storedFields().document(luceneDocId);
    }

    public String docId(int luceneDocId) throws IOException {
        return doc(luceneDocId).get("id");
    }

    @Override
    public void close() throws IOException {
        reader.close();
        dir.close();
    }
}
