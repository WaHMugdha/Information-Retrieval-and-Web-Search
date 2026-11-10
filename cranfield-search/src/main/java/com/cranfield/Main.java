package com.cranfield;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {

    static final Path DATA = Paths.get(System.getProperty("data", ".."));
    static final int K = Integer.getInteger("k", 50);
    static final float TITLE_BOOST = Float.parseFloat(System.getProperty("titleBoost", "2.0"));

    static final String[] ANALYZERS = {"standard", "standard-stop", "english", "kstem"};
    static final String[] SIMILARITIES = {"classic", "bm25", "lmd", "lmjm"};

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        switch (args[0]) {
            case "all":
                for (String an : ANALYZERS) {
                    index(an);
                    for (String sim : SIMILARITIES) {
                        batch(an, sim);
                    }
                }
                break;
            case "index":
                index(args[1]);
                break;
            case "batch":
                batch(args[1], args[2]);
                break;
            case "interactive":
                interactive(args[1], args[2]);
                break;
            default:
                usage();
        }
    }

    static Path indexDir(String analyzerName) {
        return Paths.get("indexes", analyzerName);
    }

    static void index(String analyzerName) throws Exception {
        try (Analyzer analyzer = Components.analyzer(analyzerName)) {
            int n = Indexer.build(DATA, indexDir(analyzerName), analyzer);
            System.out.println("Indexed " + n + " documents with analyzer '" + analyzerName + "'");
        }
    }

    static void batch(String analyzerName, String simSpec) throws Exception {
        if (!Files.exists(indexDir(analyzerName))) {
            System.out.println("No index for '" + analyzerName + "', building it first...");
            index(analyzerName);
        }
        List<Map<String, String>> queries = CranParser.parse(DATA.resolve("cran.qry"));
        String runName = analyzerName + "_" + simSpec.replace(':', '_');
        Path out = Paths.get("results", runName + ".txt");
        Files.createDirectories(out.getParent());

        try (Analyzer analyzer = Components.analyzer(analyzerName);
             Searcher searcher = new Searcher(indexDir(analyzerName), analyzer,
                     Components.similarity(simSpec), TITLE_BOOST);
             PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {

            int queryNumber = 0; // qrels number queries by position in cran.qry, NOT by the .I value
            for (Map<String, String> q : queries) {
                queryNumber++;
                ScoreDoc[] hits = searcher.search(q.getOrDefault("W", ""), K);
                int rank = 1;
                for (ScoreDoc h : hits) {
                    pw.printf(Locale.ROOT, "%d Q0 %s %d %.6f %s%n",
                            queryNumber, searcher.docId(h.doc), rank++, h.score, runName);
                }
            }
            System.out.println("Wrote " + out + " (" + queryNumber + " queries, top " + K + " each)");
        }
    }

    static void interactive(String analyzerName, String simSpec) throws Exception {
        if (!Files.exists(indexDir(analyzerName))) {
            index(analyzerName);
        }
        try (Analyzer analyzer = Components.analyzer(analyzerName);
             Searcher searcher = new Searcher(indexDir(analyzerName), analyzer,
                     Components.similarity(simSpec), TITLE_BOOST);
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Analyzer=" + analyzerName + "  Similarity=" + simSpec
                    + "   (empty line to quit)");
            while (true) {
                System.out.print("\nquery> ");
                String line = in.readLine();
                if (line == null || line.trim().isEmpty()) {
                    break;
                }
                ScoreDoc[] hits = searcher.search(line, 10);
                int rank = 1;
                for (ScoreDoc h : hits) {
                    Document d = searcher.doc(h.doc);
                    System.out.printf(Locale.ROOT, "%2d. doc %-5s %.4f  %s%n",
                            rank++, d.get("id"), h.score, d.get("title"));
                }
                if (hits.length == 0) {
                    System.out.println("(no results)");
                }
            }
        }
    }

    static void usage() {
        System.out.println("Usage: all | index <analyzer> | batch <analyzer> <sim> | interactive <analyzer> <sim>");
        System.out.println("Analyzers:    standard, standard-stop, english, kstem");
        System.out.println("Similarities: classic, bm25, bm25:K1:B, lmd, lmd:MU, lmjm, lmjm:LAMBDA");
    }
}
