
package com.example.fashion_ai.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.example.fashion_ai.dto.FashionRequest;
import com.example.fashion_ai.dto.OutfitRecommendation;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public OutfitRecommendation chat(FashionRequest request) {

        String template = """
                You are a professional fashion shopping assistant.

                Recommend outfits based on customer preferences.

                Customer preferences:
                Budget: {budget} rupees
                Occasion: {occasion}
                Preferred Colour: {color}
                Clothing Type: {clothingType}

                Return your response as structured data
                containing a list named outfits.

                Each outfit must contain:
                productName, category, price, description.

                Provide 3 outfit suggestions.
                Use simple language.
                Do not claim that products or prices are verified.
                """;

        PromptTemplate promptTemplate =
                new PromptTemplate(template);

        var prompt = promptTemplate.create(
                Map.of(
                        "budget", request.getBudget(),
                        "occasion", request.getOccasion(),
                        "color", request.getColor(),
                        "clothingType", request.getClothingType()
                )
        );

        return chatClient.prompt(prompt)
                .call()
                .entity(OutfitRecommendation.class);
    }
}
