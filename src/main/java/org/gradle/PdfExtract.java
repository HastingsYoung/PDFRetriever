package org.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfExtract {

	public static void main(String[] args) throws ParseException {
		try {
			// text to perform search
			String[] text = getTextFromPdf("test.pdf").split("/(\t)|(\n)|(.)/");
			
			// syntax analyzer
			Analyzer analyzer = new StandardAnalyzer();

			// optionally, use file directory to store result in file rather
			// than in memory
			Directory fileDirectory = FSDirectory.open(Paths.get("./"));
			Directory directory = new RAMDirectory();

			// create index writer, prepare configurations
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iWriter = new IndexWriter(directory, config);

			Document[] docs = new Document[text.length];
			for (int i = 0; i < text.length; i++) {
				// create empty document, assign index for documents
				Document doc = new Document();
				// fields are similar to indices, or more precisely, keywords
				doc.add(new Field("testFieleName", text[i], TextField.TYPE_STORED));
				docs[i] = doc;
			}
			// add document to index tree(the directory we create above)
			for (Document d : docs) {
				iWriter.addDocument(d);
			}
			iWriter.close();
			
			// read directory
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);

			// set query
			QueryParser parser = new QueryParser("testFieleName", analyzer);
			Query query = parser.parse("html");

			// filter results
			ScoreDoc[] hits = iSearcher.search(query, 10).scoreDocs;

			// output
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = iSearcher.doc(hits[i].doc);
				System.out.println(hitDoc.get("testFieleName"));
			}
			iReader.close();
			directory.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getTextFromPdf(String filePath) throws IOException {
		String result = null;
		FileInputStream fis = new FileInputStream(new File(filePath));
		PDDocument document = PDDocument.load(fis);
		PDFTextStripper pts = new PDFTextStripper();
		result = pts.getText(document);
		fis.close();
		document.close();

		return result;
	}
}
