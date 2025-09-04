package com.ethanrobins.chatbridge_v2.drivers;

import com.ethanrobins.chatbridge_v2.ChatBridge;
import com.ethanrobins.chatbridge_v2.Model;
import com.ethanrobins.chatbridge_v2.exceptions.HttpErrorCode;
import com.ethanrobins.chatbridge_v2.utils.RandomString;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@JsonPropertyOrder({ "model", "temperature", "max_output_tokens", "store", "prompt" })
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = Request.NoDeserialize.class)
public class Request {
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final @NotNull String id;

    private final @Nullable Model model;
    private final @Nullable Double temp;
    private final @Nullable Integer maxTokens;
    private final @Nullable Boolean store;
    private final @NotNull Prompt prompt;

    private Response response = null;

    public Request(@Nullable String id, @Nullable Model model, @Nullable Double temperature, @Nullable Integer maxTokens, @Nullable Boolean store, @NotNull Prompt prompt) {
        this.id = id != null ? id : "request_" + RandomString.generate(8, RandomString.Content.NUMBERS);
        this.model = model;
        this.temp = temperature;
        this.maxTokens = maxTokens;
        this.store = store;
        this.prompt = prompt;
    }
    public Request(@NotNull Prompt prompt) {
        this(null, null, null, null, null, prompt);
    }

    @JsonIgnore
    public @NotNull String getId() {
        return this.id;
    }
    @JsonIgnore
    public @Nullable Model getModel() {
        return this.model;
    }
    @JsonIgnore
    public @Nullable Response getResponse() {
        return this.response;
    }

    @JsonGetter("model")
    public @Nullable String getModelId() {
        return this.model != null ? this.model.getId() : null;
    }
    @JsonGetter("temperature")
    public @Nullable Double getTemperature() {
        return this.temp;
    }
    @JsonGetter("max_output_tokens")
    public @Nullable Integer getMaxTokens() {
        return this.maxTokens;
    }
    @JsonGetter("store")
    public @Nullable Boolean isStore() {
        return this.store;
    }
    @JsonGetter("prompt")
    public @NotNull Prompt getPrompt() {
        return this.prompt;
    }

    @JsonIgnore
    public CompletableFuture<Response> queue() {
        final String API_URL = ChatBridge.getSecret().get("chatgpt", "url");
        final String API_KEY = ChatBridge.getSecret().get("chatgpt", "key");

        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(this);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(API_URL))
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new HttpErrorCode(response.statusCode(), "Failed to translate the text. HTTP Error Code: " + response.statusCode() + "\n" + response.body());
                }
                String responseStr = response.body();

                System.out.println("\u001B[33m" + this.getId() + "\u001B[0m completed");

                Response data;
                try {
                    data = objectMapper.readValue(responseStr, Response.class);
                    if (data != null) data.setId(this.getId());
                } catch (JsonProcessingException ex) {
                    System.err.println("Unable to parse data: " + ex.getMessage() + "\n\nData: " + responseStr);
                    data = null;
                }
                this.response = data;
                return data;
            } catch (URISyntaxException | IOException | InterruptedException | HttpErrorCode e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @JsonPropertyOrder({ "id", "version", "variables" })
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonDeserialize(using = Request.NoDeserialize.class)
    public static class Prompt {
        private final @NotNull String id;
        private final @Nullable String version;
        private final @NotNull Map<String, String> variables = new HashMap<>();

        public Prompt(@Nullable String id, @Nullable String version, @NotNull String target, @NotNull String message) {
            this.id = id != null ? id : PromptType.MESSAGE.getId();
            this.version = version;
            this.variables.put("tgt", target);
            this.variables.put("msg", message);
        }
        public Prompt(@Nullable String id, @Nullable String version, @NotNull String target, @Nullable String message, @Nullable String title, @Nullable String author, @Nullable String description, @Nullable String footer, @Nullable Map<String, String> fields) {
            this.id = id != null ? id : PromptType.EMBED.getId();
            this.version = version;
            this.variables.put("tgt", target);
            this.variables.put("msg", message != null ? message : "");
            this.variables.put("title", title != null ? title : "");
            this.variables.put("author", author != null ? author : "");
            this.variables.put("desc", description != null ? description : "");
            this.variables.put("footer", footer != null ? footer : "");
            try {
                this.variables.put("fields", objectMapper.writeValueAsString(fields));
            } catch (JsonProcessingException e) {
                System.err.println("Unable to parse fields: " + e.getMessage());
                this.variables.put("fields", null);
            }
        }
        public Prompt(@Nullable String id, @Nullable String version, @NotNull String target, @Nullable String message, @Nullable String title, @Nullable MessageEmbed.AuthorInfo author, @Nullable String description, @Nullable MessageEmbed.Footer footer, @Nullable List<MessageEmbed.Field> fields) {
            this(id, version, target, message, title, author != null ? author.getName() : null, description, footer != null ? footer.getText() : null, getFieldsFromEmbedFields(fields));
        }
        private static Map<String, String> getFieldsFromEmbedFields(@Nullable List<MessageEmbed.Field> fields) {
            if (fields != null) {
                Map<String, String> fieldMap = new HashMap<>();
                for (MessageEmbed.Field f : fields) {
                    fieldMap.put(f.getName(), f.getValue());
                }
                if (fields.isEmpty()) fieldMap = null;
                return fieldMap;
            }
            return null;
        }

        @JsonGetter("id")
        public @NotNull String getId() {
            return this.id;
        }
        @JsonGetter("version")
        public @Nullable String getVersion() {
            return this.version;
        }
        @JsonGetter("variables")
        public @NotNull Map<String, String> getVariables() {
            return this.variables;
        }
    }

    static final class NoDeserialize extends JsonDeserializer<Request> {
        @Override
        public Request deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JsonParseException {
            throw new JsonParseException(jsonParser, "Request is serialize-only and cannot be deserialized");
        }
    }
}
