package strategy.enwiki;

import edu.jhu.nlp.wikipedia.WikiPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;

public class ENWikiIndexStrategy {

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_ID = "id";

    private final Logger logger = LogManager.getLogger();

    public Document populateDocument(Document doc, WikiPage page) {
        var title = page.getTitle().trim();
        doc.add(new TextField(FIELD_TITLE, title, Field.Store.YES));
        var text = page.getText().trim();
        doc.add(new TextField(FIELD_TEXT, text, Field.Store.YES));
        var id = Integer.parseInt(page.getID().trim());
        doc.add(new IntPoint(FIELD_ID, id));
        logger.info(String.format("[%d]\t:%s", id, title));
        return doc;
    }
}
