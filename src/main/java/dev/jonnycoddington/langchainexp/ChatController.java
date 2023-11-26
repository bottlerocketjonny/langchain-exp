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
  
  @PostMapping("/chat-with-model")
  public String chatWithModel(@RequestParam String systemMessage,
      @RequestParam String userMessage,
      @RequestParam(required = false) String modelId) {
    return chatService.chatWithModel(systemMessage, userMessage, modelId);
  }
  
  @PostMapping("/chat-with-docs")
  public String chatWithDocs(@RequestParam String question,
      @RequestParam String fileName,
      @RequestParam(required = false) String modelId,
      @RequestParam(required = false, defaultValue = "3") int maxResults) {
    return chatService.chatWithDocs(question, fileName, modelId, maxResults);
  }
}
