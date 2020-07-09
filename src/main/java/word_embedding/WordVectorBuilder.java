package word_embedding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.IOException;
import java.nio.file.Path;


public class WordVectorBuilder {

    private static final Logger logger = LogManager.getLogger();

    public WordVectorBuilder() {

    }

    public WordVectors buildFromFile(Path path) throws IOException {
        logger.info(String.format("Loading word vector from %s ...", path));
        return WordVectorSerializer.readWord2VecModel(path.toFile());
//        return WordVectorSerializer.readWordVectors(path.toFile());
    }
}
