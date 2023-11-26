package dev.jonnycoddington.langchainexp;

import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static java.time.Duration.ofSeconds;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
  
  private final Dotenv dotenv;
  
  public ChatService(Dotenv dotenv) {
    this.dotenv = dotenv;
  }
  
  public String chatWithDocs(String question, String fileName, String modelId, int maxResults) {
    ChatLanguageModel model = getChatLanguageModel(modelId);
    
    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    
    EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
        .documentSplitter(DocumentSplitters.recursive(500, 0
        , modelId == null ? new OpenAiTokenizer(GPT_3_5_TURBO) : null // TODO is there a tokenizer for HF models?
        ))
        .embeddingModel(embeddingModel)
        .embeddingStore(embeddingStore)
        .build();
    
    Document document = loadDocument(toPath(fileName));
    ingestor.ingest(document);
    
    
    PromptTemplate prompt = PromptTemplate.from(
        "Answer the following question to the best of your ability:\n"
            + "\n"
            + "Question:\n"
            + "{{question}}\n"
            + "\n"
            + "Base your answer SOLELY on the following information:\n"
            + "{{information}}\n"
            + "\n"
            + "Your answer should be clear, brief and under 5 bullet points."
    );
    
    ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
        .chatLanguageModel(model)
        .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, maxResults))
        .promptTemplate(prompt)
        .build();
    
    return chain.execute(question);
  }
  
  public String chatWithModel(String systemMessage, String userMessage, String modelId) {
    ChatLanguageModel model = getChatLanguageModel(modelId);
    
    AiMessage aiMessage = model.generate(
        systemMessage(systemMessage),
        userMessage(userMessage)
    ).content();
    
    return aiMessage.text();
  }
  
  private ChatLanguageModel getChatLanguageModel(String modelId) {
    ChatLanguageModel model;
    if (modelId == null) {
      model = OpenAiChatModel.withApiKey(dotenv.get("OPENAI_API_KEY"));
    } else {
      model = HuggingFaceChatModel.builder()
          .accessToken(dotenv.get("HF_API_KEY"))
          .modelId(modelId)
          .timeout(ofSeconds(30))
//          .returnFullText(true) // Helpful if you want to see what is being sent
          .waitForModel(true)
          .build();
    }
    return model;
  }
  
  private static Path toPath(String fileName) {
    try {
      URL fileUrl = ChatService.class.getResource("/" + fileName);
      assert fileUrl != null;
      return Paths.get(fileUrl.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  
}
