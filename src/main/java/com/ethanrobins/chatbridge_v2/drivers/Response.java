package com.ethanrobins.chatbridge_v2.drivers;

import com.ethanrobins.chatbridge_v2.Model;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonSerialize(using = Response.NoSerialize.class)
public class Response {
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private String id;
    private String openaiId;
    private long createdAt;
    private @Nullable Boolean background;
    private @Nullable OpenaiError error;
    private @Nullable Model model;
    private List<Output> output;
    private Usage usage;

    public Response() {}

    @JsonIgnore
    public void setId(@NotNull String id) {
        this.id = id;
    }
    @JsonIgnore
    public @Nullable Output getOutput() {
        return (this.output != null && !this.output.isEmpty()) ? this.output.getFirst() : null;
    }

    @JsonSetter("id")
    public void setOpenaiId(@NotNull String openaiId) {
        this.openaiId = openaiId;
    }
    @JsonSetter("created_at")
    public void setCreatedAt(long timestamp) {
        this.createdAt = timestamp;
    }
    @JsonSetter("background")
    public void setBackground(@Nullable Boolean background) {
        this.background = background;
    }
    @JsonSetter("error")
    public void setError(@Nullable OpenaiError error) {
        this.error = error;
    }
    @JsonSetter("model")
    public void setModel(@NotNull String model) {
        for (Model m : Model.values()) {
            if (m.getId().equals(model)) {
                this.model = m;
                return;
            }
        }
        this.model = null;
    }
    @JsonSetter("output")
    public void setOutputList(@NotNull List<Output> outputs) {
        this.output = outputs;
    }
    @JsonSetter("usage")
    public void setUsage(@NotNull Usage usage) {
        this.usage = usage;
    }

    @Getter
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
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonSerialize(using = Response.NoSerialize.class)
    public static class Data {
        private final @NotNull Source source;
        private final @NotNull Target target;

        @JsonCreator
        public Data(@JsonProperty("src") @NotNull Source src, @JsonProperty("tgt") @NotNull Target tgt) {
            this.source = src;
            this.target = tgt;
        }

        @Getter
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class Source {
            protected final @NotNull String tag;
            protected final @NotNull String lang;

            @JsonCreator
            public Source(@JsonProperty("tag") @NotNull String tag, @JsonProperty("lang") @NotNull String lang) {
                this.tag = tag;
                this.lang = lang;
            }
        }

        @Getter
        @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        @JsonSerialize(using = Response.NoSerialize.class)
        public static class Target extends Source {
            private final @NotNull String explicit;
            private final @NotNull String safe;

            @JsonCreator
            public Target(@JsonProperty("tag") @NotNull String tag, @JsonProperty("lang") @NotNull String lang, @JsonProperty("e") @NotNull String explicit, @JsonProperty("s") @NotNull String safe) {
                super(tag, lang);
                this.explicit = explicit;
                this.safe = safe;
            }
        }
    }

    @Getter
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
