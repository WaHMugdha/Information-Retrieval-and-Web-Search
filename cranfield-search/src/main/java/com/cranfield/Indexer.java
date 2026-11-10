package com.cranfield;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Indexer {

    public static int build(Path dataDir, Path indexDir, Analyzer analyzer) throws IOException {
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        cfg.setSimilarity(new BM25Similarity());

        int count = 0;
        try (Directory dir = FSDirectory.open(indexDir);
             IndexWriter writer = new IndexWriter(dir, cfg)) {
            for (Map<String, String> r : CranParser.parse(dataDir.resolve("cran.all.1400"))) {
                Document d = new Document();
                d.add(new StringField("id", r.get("I"), Field.Store.YES));
                d.add(new TextField("title", r.getOrDefault("T", ""), Field.Store.YES));
                d.add(new TextField("author", r.getOrDefault("A", ""), Field.Store.YES));
                d.add(new TextField("bib", r.getOrDefault("B", ""), Field.Store.YES));
                d.add(new TextField("content", r.getOrDefault("W", ""), Field.Store.YES));
                writer.addDocument(d);
                count++;
            }
        }
        return count;
    }
}
