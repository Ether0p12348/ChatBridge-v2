package com.ethanrobins.chatbridge_v2.drivers;

import com.ethanrobins.chatbridge_v2.Model;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonSerialize(using = Response.NoSerialize.class)
public class Response {
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    private String id = null;
    private final @NotNull String openaiId;
    private final long createdAt;
    private final @Nullable Boolean background;
    private final @Nullable OpenaiError error;
    private final @Nullable Model model;
    private final @NotNull List<Output> output;
    private final @NotNull Usage usage;

    @JsonCreator
    public Response(@JsonProperty("id") @NotNull String openaiId, @JsonProperty("created_at") long timestamp, @JsonProperty("background") @Nullable Boolean background, @JsonProperty("error") @Nullable OpenaiError error, @JsonProperty("model") @Nullable String modelStr, @JsonProperty("output") @NotNull List<Output> output, @JsonProperty("usage") @NotNull Usage usage) {
        this.openaiId = openaiId;
        this.createdAt = timestamp;
        this.background = background;
        this.error = error;
        this.output = output;
        this.usage = usage;

        Model modelVal = null;
        for (Model m : Model.values()) {
            if (m.getId().equals(modelStr)) {
                modelVal = m;
                break;
            }
        }
        this.model = modelVal;
    }

    @JsonIgnore
    public void setId(@NotNull String id) {
        if (this.id != null) {
            System.err.println("Warning: Response#setId() called more than once. Ignoring call.");
            return;
        }
        this.id = id;
    }

    @JsonIgnore
    public @Nullable Output getOutput() {
        return (!this.output.isEmpty()) ? this.output.getFirst() : null;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonSerialize(using = Response.NoSerialize.class)
    public static class OpenaiError {
        private final @NotNull String code;
        private final @NotNull String message;

        @JsonCreator
        public OpenaiError(@JsonProperty("code") @NotNull String code, @JsonProperty("message") @NotNull String message) {
            this.code = code;
            this.message = message;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonSerialize(using = Response.NoSerialize.class)
    public static class Output {
        private final @NotNull String id;
        private final @NotNull String status;
        private final @NotNull List<Content> content;

        @JsonCreator
        public Output(@JsonProperty("id") @NotNull String id, @JsonProperty("status") @NotNull String status , @JsonProperty("content") @NotNull List<Content> content) {
            this.id = id;
            this.status = status;
            this.content = content;
        }
        @JsonIgnore
        public @Nullable Content getContent() {
            return (!this.content.isEmpty()) ? this.content.getFirst() : null;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class Content {
            public final Data data;

            @JsonCreator
            public Content(@JsonProperty("text") @NotNull String text) {
                Data data;
                try {
                    data = objectMapper.readValue(text, Response.Data.class);
                } catch (JsonProcessingException ex) {
                    System.err.println("Unable to parse data: " + ex.getMessage() + "\n\nData: " + text);
                    data = null;
                }
                this.data = data;
            }
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonSerialize(using = Response.NoSerialize.class)
    public static class Data {
        private final @NotNull Source source;
        private final @NotNull Target<?> target;

        @JsonCreator
        public Data(@JsonProperty("src") @NotNull Source src, @JsonProperty("tgt") @NotNull @JsonDeserialize(using = Data.TargetDeserializer.class) Target<?> tgt) {
            this.source = src;
            this.target = tgt;
        }

        public interface SourceFields {
            @JsonIgnore
            String getTag();
            @JsonIgnore
            String getLang();
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class Source implements SourceFields {
            protected final @NotNull String tag;
            protected final @NotNull String lang;

            @JsonCreator
            public Source(@JsonProperty("tag") @NotNull String tag, @JsonProperty("lang") @NotNull String lang) {
                this.tag = tag;
                this.lang = lang;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(using = Response.NoSerialize.class)
        public interface Target<T> extends SourceFields  {
            @JsonIgnore
            T getExplicit();
            @JsonIgnore
            T getSafe();
            @JsonIgnore
            T getBySafetyLevel(@NotNull SafetyLevel safetyLevel);
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class MessageTarget extends Source implements Target<String> {
            private final @NotNull String explicit;
            private final @NotNull String safe;

            @JsonCreator
            public MessageTarget(@JsonProperty("tag") @NotNull String tag, @JsonProperty("lang") @NotNull String lang, @JsonProperty("e") @NotNull String explicit, @JsonProperty("s") @NotNull String safe) {
                super(tag, lang);
                this.explicit = explicit;
                this.safe = safe;
            }

            @JsonIgnore
            @Override
            public String getBySafetyLevel(@NotNull SafetyLevel safetyLevel) {
                if (safetyLevel == SafetyLevel.EXPLICIT) {
                    return this.explicit;
                }

                return this.safe;
            }
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class EmbedTarget extends Source implements Target<EmbedContent> {
            private final @NotNull EmbedContent explicit;
            private final @NotNull EmbedContent safe;

            @JsonCreator
            public EmbedTarget(@JsonProperty("tag") @NotNull String tag, @JsonProperty("lang") @NotNull String lang, @JsonProperty("e") @NotNull EmbedContent explicit, @JsonProperty("s") @NotNull EmbedContent safe) {
                super(tag, lang);
                this.explicit = explicit;
                this.safe = safe;
            }

            @JsonIgnore
            @Override
            public EmbedContent getBySafetyLevel(@NotNull SafetyLevel safetyLevel) {
                if (safetyLevel == SafetyLevel.EXPLICIT) {
                    return this.explicit;
                }

                return this.safe;
            }
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class EmbedContent {
            private final @Nullable String message;
            private final @Nullable String title;
            private final @Nullable String author;
            private final @Nullable String description;
            private final @Nullable String footer;
            private final @Nullable List<Field> fields;

            @JsonCreator
            public EmbedContent(@JsonProperty("msg") @Nullable String message, @JsonProperty("title") @Nullable String title, @JsonProperty("author") @Nullable String author, @JsonProperty("desc") @Nullable String description, @JsonProperty("footer") @Nullable String footer, @JsonProperty("fields") @Nullable List<Field> fields) {
                this.message = (message == null || message.isEmpty()) ? null : message;
                this.title = (title == null || title.isEmpty()) ? null : title;
                this.author = (author == null || author.isEmpty()) ? null : author;
                this.description = (description == null || description.isEmpty()) ? null : description;
                this.footer = (footer == null || footer.isEmpty()) ? null : footer;
                this.fields = (fields == null || fields.isEmpty()) ? null : fields;
            }

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
            @JsonSerialize(using = Response.NoSerialize.class)
            public static class Field {
                private final @Nullable String name;
                private final @Nullable String value;

                @JsonCreator
                public Field(@JsonProperty("name") @Nullable String name, @JsonProperty("value") @Nullable String value) {
                    this.name = (name == null || name.isEmpty()) ? "" : name;
                    this.value = (value == null || value.isEmpty()) ? "" : value;
                }
            }
        }

        public static class TargetDeserializer extends StdDeserializer<Target> {
            public TargetDeserializer() {
                super(Target.class);
            }

            @Override
            public Target deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                final ObjectMapper mapper = (ObjectMapper) p.getCodec();
                final JsonNode node = mapper.readTree(p);

                JsonNode e = node.get("e");
                JsonNode s = node.get("s");

                if (e == null || s == null) {
                    ctxt.reportInputMismatch(Target.class, "Both 'e' and 's' must be present on 'tgt'. Found e=%s, s=%s", typeOf(e), typeOf(s));
                }

                boolean bothText = e.isTextual() && s.isTextual();
                boolean bothObj  = e.isObject()  && s.isObject();

                if (bothText)  return mapper.treeToValue(node, MessageTarget.class);
                if (bothObj)   return mapper.treeToValue(node, EmbedTarget.class);

                ctxt.reportInputMismatch(Target.class,
                        "'e' and 's' must be the same kind (both strings for MessageTarget, or both objects for EmbedTarget). Found e=%s, s=%s",
                        typeOf(e), typeOf(s));
                return null;
            }

            private static String typeOf(JsonNode node) {
                return (node == null) ? "null" : node.getNodeType().toString().toLowerCase();
            }
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonSerialize(using = Response.NoSerialize.class)
    public static class Usage {
        private final int input;
        private final int output;
        private final int total;

        @JsonCreator
        public Usage(@JsonProperty("input_tokens") int input, @JsonProperty("output_tokens") int output, @JsonProperty("total_tokens") int total) {
            this.input = input;
            this.output = output;
            this.total = total;
        }
    }

    public static final class NoSerialize extends JsonSerializer<Response> {
        @Override
        public void serialize(Response response, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws JsonMappingException {
            throw new JsonMappingException(jsonGenerator, "Response is deserialize-only and cannot be serialized");
        }
    }
}
