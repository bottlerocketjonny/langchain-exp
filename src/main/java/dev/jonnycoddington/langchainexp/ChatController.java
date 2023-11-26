package dev.jonnycoddington.langchainexp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0.1")
public class ChatController {
  
  private final ChatService chatService;
  
  @Autowired
  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }
  
  /**
   * Chat with an LLM.
   *
   * @param systemMessage The system message with a description of how you want the LLM to act
   * @param userMessage The user message containing the question
   * @param modelId The id of the HuggingFace model to use. If null, this will default to OpenAI's 'gpt-3.5-turbo' model.
   * @return The response from the LLM
   */
  @PostMapping("/chat-with-model")
  public String chatWithModel(@RequestParam String systemMessage,
      @RequestParam String userMessage,
      @RequestParam(required = false) String modelId) {
    return chatService.chatWithModel(systemMessage, userMessage, modelId);
  }
  
  /**
   * Interact with an LLM using your provided document as context via an embedding.
   *
   * @param question The question to ask the LLM
   * @param fileName The name of the file you'd like to use as an embedding
   * @param modelId The id of the HuggingFace model to use. If null, this will default to the OpenAI 'gpt-3.5-turbo' model
   * @param maxResults The maximum number of text segments retrieved from the embedding store
   * @return The response from the LLM
   */
  @PostMapping("/chat-with-docs")
  public String chatWithDocs(@RequestParam String question,
      @RequestParam String fileName,
      @RequestParam(required = false) String modelId,
      @RequestParam(required = false, defaultValue = "3") int maxResults) {
    return chatService.chatWithDocs(question, fileName, modelId, maxResults);
  }
}
