package com.ethanrobins.chatbridge_v2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Modified version of {@link EmbedBuilder} originally licensed under Apache 2.0.
 * 
 * <br><br><b>MessageEmbed Data</b>
 * <br>{@code +} = gets translated<br>{@code -} = not updatable
 * <table>
 *     <tr>
 *         <td>url</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>title</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>description</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>type</td>
 *         <td>-</td>
 *         <td>{@link net.dv8tion.jda.api.entities.EmbedType}</td>
 *     </tr>
 *     <tr>
 *         <td>timestamp</td>
 *         <td></td>
 *         <td>{@link java.time.OffsetDateTime} &larr; {@link java.time.temporal.TemporalAccessor}</td>
 *     </tr>
 *     <tr>
 *         <td>color</td>
 *         <td></td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>thumbnail</td>
 *         <td></td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; url</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; proxyUrl</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; width</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; height</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>siteProvider</td>
 *         <td>-</td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.Provider}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; name</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; url</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>author</td>
 *         <td></td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; name</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; url</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; iconUrl</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>videoInfo</td>
 *         <td>-</td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.VideoInfo}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; url</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; proxyUrl</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; width</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; height</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>footer</td>
 *         <td></td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.Footer}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; text</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; icon</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; proxyIconUrl</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>image</td>
 *         <td></td>
 *         <td>{@link net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; url</td>
 *         <td></td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; proxyUrl</td>
 *         <td>-</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; width</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; height</td>
 *         <td>-</td>
 *         <td>{@code int}</td>
 *     </tr>
 *     <tr>
 *         <td>fields</td>
 *         <td></td>
 *         <td>{@link java.util.List}{@code <}{@link net.dv8tion.jda.api.entities.MessageEmbed.Field}{@code >}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; name</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; value</td>
 *         <td>+</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&rdsh; inline</td>
 *         <td></td>
 *         <td>{@code boolean}</td>
 *     </tr>
 * </table>
 *
 * <a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/01-Overview.png" target="_blank">Embed Visualization</a>
 *
 * @see EmbedBuilder
 * @see MessageEmbed
 * */
public class TranslateEmbedBuilder {
    private String batchIdNum = ChatBridge.genId(8);
    private String batchId = "batch_" + batchIdNum;
    private Batch batch = new Batch(batchId);

    private final List<MessageEmbed.Field> fields = new ArrayList<>();
    private final StringBuilder description = new StringBuilder();
    private int color = Role.DEFAULT_COLOR_RAW;
    private String url, title;
    private OffsetDateTime timestamp;
    private MessageEmbed.Thumbnail thumbnail;
    private MessageEmbed.AuthorInfo author;
    private MessageEmbed.Footer footer;
    private MessageEmbed.ImageInfo image;

    private boolean translateAuthor = true;
    private boolean translateTitle= true;
    private boolean translateDescription = true;
    private boolean translateFooter = true;
    private boolean translateFields = true;

    /**
     * Constructs a new TranslateEmbedBuilder instance, which can be used to create {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds}.
     * These can then be sent to a channel using {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)}.
     * <br>Every part of an embed can be removed or cleared by providing {@code null} to the setter method.
     */
    public TranslateEmbedBuilder() { }

    /**
     * Creates an TranslateEmbedBuilder using fields from an existing translate builder
     *
     * @param  builder
     *         the existing builder
     */
    public TranslateEmbedBuilder(@Nullable TranslateEmbedBuilder builder)
    {
        copyFrom(builder);
    }

    /**
     * Creates an TranslateEmbedBuilder using fields in an existing embed.
     *
     * @param  embed
     *         the existing embed
     */
    public TranslateEmbedBuilder(@Nullable MessageEmbed embed)
    {
        copyFrom(embed);
    }

    /**
     * Creates an instance of this builder from the provided {@link DataObject}.
     *
     * <p>This is the inverse of {@link MessageEmbed#toData()}.
     *
     * @param  data
     *         The serialized embed object
     *
     * @throws IllegalArgumentException
     *         If the provided data is {@code null} or invalid
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided data is malformed
     *
     * @return The new builder instance
     */
    @NotNull
    public static TranslateEmbedBuilder fromData(@NotNull DataObject data)
    {
        Checks.notNull(data, "DataObject");
        TranslateEmbedBuilder builder = new TranslateEmbedBuilder();

        builder.setTitle(data.getString("title", null));
        builder.setUrl(data.getString("url", null));
        builder.setDescription(data.getString("description", ""));
        builder.setTimestamp(data.isNull("timestamp") ? null : OffsetDateTime.parse(data.getString("timestamp")));
        builder.setColor(data.getInt("color", Role.DEFAULT_COLOR_RAW));

        data.optObject("thumbnail").ifPresent(thumbnail ->
                builder.setThumbnail(thumbnail.getString("url"))
        );

        data.optObject("author").ifPresent(author ->
                builder.setAuthor(
                        author.getString("name", ""),
                        author.getString("url", null),
                        author.getString("icon_url", null)
                )
        );

        data.optObject("footer").ifPresent(footer ->
                builder.setFooter(
                        footer.getString("text", ""),
                        footer.getString("icon_url", null)
                )
        );

        data.optObject("image").ifPresent(image ->
                builder.setImage(image.getString("url"))
        );

        data.optArray("fields").ifPresent(arr ->
                arr.stream(DataArray::getObject).forEach(field ->
                        builder.addField(
                                field.getString("name", EmbedBuilder.ZERO_WIDTH_SPACE),
                                field.getString("value", EmbedBuilder.ZERO_WIDTH_SPACE),
                                field.getBoolean("inline", false)
                        )
                )
        );

        return builder;
    }

    /**
     * Returns a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     * that has been checked as being valid for sending.
     *
     * @throws java.lang.IllegalStateException
     *         <ul>
     *             <li>If the embed is empty. Can be checked with {@link #isEmpty()}.</li>
     *             <li>If the character limit for {@code description}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the embed's total length, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#EMBED_MAX_LENGTH_BOT} as {@value net.dv8tion.jda.api.entities.MessageEmbed#EMBED_MAX_LENGTH_BOT},
     *             is exceeded.</li>
     *             <li>If the embed's number of embed fields, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#MAX_FIELD_AMOUNT} as {@value net.dv8tion.jda.api.entities.MessageEmbed#MAX_FIELD_AMOUNT},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return the built, sendable {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    @NotNull
    public MessageEmbed build()
    {
        if (isEmpty())
            throw new IllegalStateException("Cannot build an empty embed!");
        if (description.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH)
            throw new IllegalStateException(Helpers.format("Description is longer than %d! Please limit your input!", MessageEmbed.DESCRIPTION_MAX_LENGTH));
        if (length() > MessageEmbed.EMBED_MAX_LENGTH_BOT)
            throw new IllegalStateException(Helpers.format("Cannot build an embed with more than %d characters!", MessageEmbed.EMBED_MAX_LENGTH_BOT));
        if (fields.size() > MessageEmbed.MAX_FIELD_AMOUNT)
            throw new IllegalStateException(Helpers.format("Cannot build an embed with more than %d embed fields set!", MessageEmbed.MAX_FIELD_AMOUNT));
        final String description = this.description.length() < 1 ? null : this.description.toString();

        return EntityBuilder.createMessageEmbed(url, title, description, EmbedType.RICH, timestamp,
                color, thumbnail, null, author, null, footer, image, new LinkedList<>(fields));
    }

    /**
     * Resets this builder to default state.
     * <br>All parts will be either empty or null after this method has returned.
     *
     * @return The current TranslateEmbedBuilder with default values
     */
    @NotNull
    public TranslateEmbedBuilder clear()
    {
        batchIdNum = ChatBridge.genId(8);
        batchId = "batch_" + batchIdNum;
        batch = new Batch(batchId);

        description.setLength(0);
        fields.clear();
        url = null;
        title = null;
        timestamp = null;
        color = Role.DEFAULT_COLOR_RAW;
        thumbnail = null;
        author = null;
        footer = null;
        image = null;
        return this;
    }

    /**
     * Copies the data from the given builder into this builder.
     * <br>All the parts of the given builder will be applied to this one.
     *
     * @param  builder
     *         the existing builder
     */
    public void copyFrom(@Nullable TranslateEmbedBuilder builder)
    {
        if (builder != null)
        {
            setDescription(builder.description.toString());
            this.clearFields();
            this.fields.addAll(builder.fields);
            this.url = builder.url;
            this.title = builder.title;
            this.timestamp = builder.timestamp;
            this.color = builder.color;
            this.thumbnail = builder.thumbnail;
            this.author = builder.author;
            this.footer = builder.footer;
            this.image = builder.image;
        }
    }

    /**
     * Copies the data from the given embed into this builder.
     * <br>All the parts of the given embed will be applied to this builder.
     *
     * @param  embed
     *         the existing embed
     */
    public void copyFrom(@Nullable MessageEmbed embed)
    {
        if(embed != null)
        {
            setDescription(embed.getDescription());
            this.clearFields();
            this.fields.addAll(embed.getFields());
            this.url = embed.getUrl();
            this.title = embed.getTitle();
            this.timestamp = embed.getTimestamp();
            this.color = embed.getColorRaw();
            this.thumbnail = embed.getThumbnail();
            this.author = embed.getAuthor();
            this.footer = embed.getFooter();
            this.image = embed.getImage();
        }
    }

    /**
     * Checks if the given embed is empty. Empty embeds will throw an exception if built.
     *
     * @return true if the embed is empty and cannot be built
     */
    public boolean isEmpty()
    {
        return (title == null || title.trim().isEmpty())
                && timestamp == null
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && color == Role.DEFAULT_COLOR_RAW
                && description.length() == 0
                && fields.isEmpty();
    }

    /**
     * The overall length of the current TranslateEmbedBuilder in displayed characters.
     * <br>Represents the {@link net.dv8tion.jda.api.entities.MessageEmbed#getLength() MessageEmbed.getLength()} value.
     *
     * @return length of the current builder state
     */
    public int length()
    {
        int length = description.toString().trim().length();
        synchronized (fields)
        {
            length = fields.stream().map(f -> f.getName().length() + f.getValue().length()).reduce(length, Integer::sum);
        }
        if (title != null)
            length += title.length();
        if (author != null)
            length += author.getName().length();
        if (footer != null)
            length += footer.getText().length();
        return length;
    }

    /**
     * Checks whether the constructed {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     * is within the limits for a bot account.
     *
     * @return True, if the {@link #length() length} is less or equal to {@value net.dv8tion.jda.api.entities.MessageEmbed#EMBED_MAX_LENGTH_BOT}
     *
     * @see    MessageEmbed#EMBED_MAX_LENGTH_BOT
     */
    public boolean isValidLength()
    {
        final int length = length();
        return length <= MessageEmbed.EMBED_MAX_LENGTH_BOT;
    }

    /**
     * Sets the Title of the embed.
     * <br>Overload for {@link #setTitle(String, String)} without URL parameter.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/04-setTitle.png">Example</a></b>
     *
     * @param  title
     *         the title of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code title} is an empty String.</li>
     *             <li>If the character limit for {@code title}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return the builder after the title has been set
     */
    @NotNull
    public TranslateEmbedBuilder setTitle(@Nullable String title)
    {
        return setTitle(title, null);
    }

    /**
     * Sets the Title of the embed.
     * <br>You can provide {@code null} as url if no url should be used.
     * <br>If you want to set a URL without a title, use {@link #setUrl(String)} instead.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/04-setTitle.png">Example</a></b>
     *
     * @param  title
     *         the title of the embed
     * @param  url
     *         Makes the title into a hyperlink pointed at this url.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code title} is an empty String.</li>
     *             <li>If the character limit for {@code title}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the title has been set
     */
    @NotNull
    public TranslateEmbedBuilder setTitle(@Nullable String title, @Nullable String url)
    {
        return setTitle(title, url, true);
    }

    @NotNull
    public TranslateEmbedBuilder setTitle(@Nullable String title, @Nullable String url, boolean translate)
    {
        this.translateTitle = translate;
        if (title == null)
        {
            this.title = null;
            this.url = null;
        }
        else
        {
            Checks.notEmpty(title, "Title");
            Checks.check(title.length() <= MessageEmbed.TITLE_MAX_LENGTH, "Title cannot be longer than %d characters.", MessageEmbed.TITLE_MAX_LENGTH);
            if (Helpers.isBlank(url))
                url = null;
            urlCheck(url);

            this.title = title;
            this.url = url;
        }
        return this;
    }

    /**
     * Sets the URL of the embed.
     * <br>The Discord client mostly only uses this property in combination with the {@link #setTitle(String) title} for a clickable Hyperlink.
     *
     * <p>If multiple embeds in a message use the same URL, the Discord client will merge them into a single embed and aggregate images into a gallery view.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the URL has been set
     *
     * @see    #setTitle(String, String)
     */
    @NotNull
    public TranslateEmbedBuilder setUrl(@Nullable String url)
    {
        if (Helpers.isBlank(url))
            url = null;
        urlCheck(url);
        this.url = url;

        return this;
    }

    /**
     * The {@link java.lang.StringBuilder StringBuilder} used to
     * build the description for the embed.
     * <br>Note: To reset the description use {@link #setDescription(CharSequence) setDescription(null)}
     *
     * @return StringBuilder with current description context
     */
    @NotNull
    public StringBuilder getDescriptionBuilder()
    {
        return description;
    }

    /**
     * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/05-setDescription.png">Example</a></b>
     *
     * @param  description
     *         the description of the embed, {@code null} to reset
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@code description} is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH} characters,
     *         as defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH}
     *
     * @return the builder after the description has been set
     */
    @NotNull
    public final TranslateEmbedBuilder setDescription(@Nullable CharSequence description)
    {
        return setDescription(description, true);
    }

    @NotNull
    public final TranslateEmbedBuilder setDescription(@Nullable CharSequence description, boolean translate)
    {
        this.translateDescription = translate;
        this.description.setLength(0);
        if (description != null && description.length() >= 1)
            appendDescription(description);
        return this;
    }

    /**
     * Appends to the description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/05-setDescription.png">Example</a></b>
     *
     * @param  description
     *         the string to append to the description of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code description} String is null.</li>
     *             <li>If the character limit for {@code description}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return the builder after the description has been set
     */
    @NotNull
    public TranslateEmbedBuilder appendDescription(@NotNull CharSequence description)
    {
        Checks.notNull(description, "description");
        Checks.check(this.description.length() + description.length() <= MessageEmbed.DESCRIPTION_MAX_LENGTH,
                "Description cannot be longer than %d characters.", MessageEmbed.DESCRIPTION_MAX_LENGTH);
        this.description.append(description);
        return this;
    }

    @NotNull
    public TranslateEmbedBuilder doTranslateDescription(boolean translateDescription) {
        this.translateDescription = translateDescription;
        return this;
    }

    /**
     * Sets the Timestamp of the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/13-setTimestamp.png">Example</a></b>
     *
     * <p><b>Hint:</b> You can get the current time using {@link java.time.Instant#now() Instant.now()} or convert time from a
     * millisecond representation by using {@link java.time.Instant#ofEpochMilli(long) Instant.ofEpochMilli(long)};
     *
     * @param  temporal
     *         the temporal accessor of the timestamp
     *
     * @return the builder after the timestamp has been set
     */
    @NotNull
    public TranslateEmbedBuilder setTimestamp(@Nullable TemporalAccessor temporal)
    {
        this.timestamp = Helpers.toOffsetDateTime(temporal);
        return this;
    }

    /**
     * Sets the Color of the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/02-setColor.png" target="_blank">Example</a></b>
     *
     * @param  color
     *         The {@link java.awt.Color Color} of the embed
     *         or {@code null} to use no color
     *
     * @return the builder after the color has been set
     *
     * @see    #setColor(int)
     */
    @NotNull
    public TranslateEmbedBuilder setColor(@Nullable Color color)
    {
        this.color = color == null ? Role.DEFAULT_COLOR_RAW : color.getRGB();
        return this;
    }

    /**
     * Sets the raw RGB color value for the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/02-setColor.png" target="_blank">Example</a></b>
     *
     * @param  color
     *         The raw rgb value, or {@link Role#DEFAULT_COLOR_RAW} to use no color
     *
     * @return the builder after the color has been set
     *
     * @see    #setColor(java.awt.Color)
     */
    @NotNull
    public TranslateEmbedBuilder setColor(int color)
    {
        this.color = color;
        return this;
    }

    /**
     * Sets the Thumbnail of the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/06-setThumbnail.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(net.dv8tion.jda.api.utils.FileUpload...) MessageChannel.sendFiles(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * TranslateEmbedBuilder embed = new TranslateEmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setThumbnail("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  url
     *         the url of the thumbnail of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the thumbnail has been set
     */
    @NotNull
    public TranslateEmbedBuilder setThumbnail(@Nullable String url)
    {
        if (url == null)
        {
            this.thumbnail = null;
        }
        else
        {
            urlCheck(url);
            this.thumbnail = new MessageEmbed.Thumbnail(url, null, 0, 0);
        }
        return this;
    }

    /**
     * Sets the Image of the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/11-setImage.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(net.dv8tion.jda.api.utils.FileUpload...) MessageChannel.sendFiles(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * TranslateEmbedBuilder embed = new TranslateEmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  url
     *         the url of the image of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the image has been set
     *
     * @see    net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(net.dv8tion.jda.api.utils.FileUpload...) MessageChannel.sendFiles(...)
     */
    @NotNull
    public TranslateEmbedBuilder setImage(@Nullable String url)
    {
        if (url == null)
        {
            this.image = null;
        }
        else
        {
            urlCheck(url);
            this.image = new MessageEmbed.ImageInfo(url, null, 0, 0);
        }
        return this;
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png">Example</a></b>
     *
     * @param  name
     *         the name of the author of the embed. If this is not set, the author will not appear in the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@code name} is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH} characters,
     *         as defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH}
     *
     * @return the builder after the author has been set
     */
    @NotNull
    public TranslateEmbedBuilder setAuthor(@Nullable String name)
    {
        return setAuthor(name, null, null);
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name and the url.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png">Example</a></b>
     *
     * @param  name
     *         the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param  url
     *         the url of the author of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code name}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the author has been set
     */
    @NotNull
    public TranslateEmbedBuilder setAuthor(@Nullable String name, @Nullable String url)
    {
        return setAuthor(name, url, null);
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(net.dv8tion.jda.api.utils.FileUpload...) MessageChannel.sendFiles(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * TranslateEmbedBuilder embed = new TranslateEmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setAuthor("Minn", null, "attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  name
     *         the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param  url
     *         the url of the author of the embed
     * @param  iconUrl
     *         the url of the icon for the author
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code name}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code url}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *             <li>If the character limit for {@code iconUrl}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the author has been set
     */
    @NotNull
    public TranslateEmbedBuilder setAuthor(@Nullable String name, @Nullable String url, @Nullable String iconUrl)
    {
        return setAuthor(name, url, iconUrl, true);
    }

    @NotNull
    public TranslateEmbedBuilder setAuthor(@Nullable String name, @Nullable String url, @Nullable String iconUrl, boolean translate)
    {
        this.translateAuthor = translate;
        // We only check if the name is null because its presence is what determines if the author will appear in the embed.
        if (name == null)
        {
            this.author = null;
        }
        else
        {
            Checks.notLonger(name, MessageEmbed.AUTHOR_MAX_LENGTH, "Name");
            urlCheck(url);
            urlCheck(iconUrl);
            this.author = new MessageEmbed.AuthorInfo(name, url, iconUrl, null);
        }
        return this;
    }

    /**
     * Sets the Footer of the embed without icon.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/12-setFooter.png">Example</a></b>
     *
     * @param  text
     *         the text of the footer of the embed. If this is not set or set to null, the footer will not appear in the embed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@code text} is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH} characters,
     *         as defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH}
     *
     * @return the builder after the footer has been set
     */
    @NotNull
    public TranslateEmbedBuilder setFooter(@Nullable String text)
    {
        return setFooter(text, null);
    }

    /**
     * Sets the Footer of the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/12-setFooter.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(net.dv8tion.jda.api.utils.FileUpload...) MessageChannel.sendFiles(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * TranslateEmbedBuilder embed = new TranslateEmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setFooter("Cool footer!", "attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  text
     *         the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
     * @param  iconUrl
     *         the url of the icon for the footer
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the character limit for {@code text}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code iconUrl}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the footer has been set
     */
    @NotNull
    public TranslateEmbedBuilder setFooter(@Nullable String text, @Nullable String iconUrl)
    {
        return setFooter(text, iconUrl, true);
    }

    @NotNull
    public TranslateEmbedBuilder setFooter(@Nullable String text, @Nullable String iconUrl, boolean translate) {
        this.translateFooter = translate;
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null)
        {
            this.footer = null;
        }
        else
        {
            Checks.notLonger(text, MessageEmbed.TEXT_MAX_LENGTH, "Text");
            urlCheck(iconUrl);
            this.footer = new MessageEmbed.Footer(text, iconUrl, null);
        }
        return this;
    }

    /**
     * Copies the provided Field into a new Field for this builder.
     * <br>For additional documentation, see {@link #addField(String, String, boolean)}
     *
     * @param  field
     *         the field object to add
     *
     * @return the builder after the field has been added
     */
    @NotNull
    public TranslateEmbedBuilder addField(@Nullable MessageEmbed.Field field)
    {
        return field == null ? this : addField(field.getName(), field.getValue(), field.isInline());
    }

    /**
     * Adds a Field to the embed.
     *
     * <p>Note: If a blank string is provided to either {@code name} or {@code value}, the blank string is replaced
     * with {@link net.dv8tion.jda.api.EmbedBuilder#ZERO_WIDTH_SPACE}.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/07-addField.png">Example of Inline</a></b>
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/08-addField.png">Example of Non-inline</a></b>
     *
     * @param  name
     *         the name of the Field, displayed in bold above the {@code value}.
     * @param  value
     *         the contents of the field.
     * @param  inline
     *         whether or not this field should display inline.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If the character limit for {@code name}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code value}, defined by {@link net.dv8tion.jda.api.entities.MessageEmbed#VALUE_MAX_LENGTH} as {@value net.dv8tion.jda.api.entities.MessageEmbed#VALUE_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return the builder after the field has been added
     */
    @NotNull
    public TranslateEmbedBuilder addField(@NotNull String name, @NotNull String value, boolean inline)
    {
        Checks.notNull(name, "Name");
        Checks.notNull(value, "Value");
        this.fields.add(new MessageEmbed.Field(name, value, inline));
        return this;
    }

    /**
     * Adds a blank (empty) Field to the embed.
     *
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/07-addField.png">Example of Inline</a></b>
     * <p><b><a href="https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/08-addField.png">Example of Non-inline</a></b>
     *
     * @param  inline
     *         whether or not this field should display inline
     *
     * @return the builder after the field has been added
     */
    @NotNull
    public TranslateEmbedBuilder addBlankField(boolean inline)
    {
        this.fields.add(new MessageEmbed.Field(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, inline));
        return this;
    }

    /**
     * Clears all fields from the embed, such as those created with the
     * {@link TranslateEmbedBuilder#TranslateEmbedBuilder(net.dv8tion.jda.api.entities.MessageEmbed) TranslateEmbedBuilder(MessageEmbed)}
     * constructor or via the
     * {@link TranslateEmbedBuilder#addField(net.dv8tion.jda.api.entities.MessageEmbed.Field) addField} methods.
     *
     * @return the builder after the field has been added
     */
    @NotNull
    public TranslateEmbedBuilder clearFields()
    {
        this.fields.clear();
        return this;
    }

    @NotNull
    public TranslateEmbedBuilder doTranslateFields(boolean translateFields) {
        this.translateFields = translateFields;
        return this;
    }

    /**
     * <b>Modifiable</b> list of {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} Fields that the builder will
     * use for {@link #build()}.
     * <br>You can add/remove Fields and restructure this {@link java.util.List List} and it will then be applied in the
     * built MessageEmbed. These fields will be available again through {@link net.dv8tion.jda.api.entities.MessageEmbed#getFields() MessageEmbed.getFields()}.
     *
     * @return Mutable List of {@link net.dv8tion.jda.api.entities.MessageEmbed.Field Fields}
     */
    @NotNull
    public List<MessageEmbed.Field> getFields()
    {
        return fields;
    }

    private void urlCheck(@Nullable String url)
    {
        if (url != null)
        {
            Checks.notLonger(url, MessageEmbed.URL_MAX_LENGTH, "URL");
            Checks.check(EmbedBuilder.URL_PATTERN.matcher(url).matches(), "URL must be a valid http(s) or attachment url.");
        }
    }

    public TranslateEmbedBuilder translate(@NotNull String targetLanguage){
        if (this.translateTitle) {
            if (this.title != null && !this.title.isEmpty()) {
                this.batch.add(new Payload(this.batchIdNum + "_title", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + this.title, 5000));
            }
        }
        if (this.translateDescription) {
            if (!this.description.isEmpty()) {
                this.batch.add(new Payload(this.batchIdNum + "_description", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + this.description, 5000));
            }
        }
        if (this.translateFooter) {
            if (this.footer != null && this.footer.getText() != null && !this.footer.getText().isEmpty()) {
                this.batch.add(new Payload(this.batchIdNum + "_footer_text", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + this.footer.getText(), 5000));
            }
        }
        if (this.translateAuthor) {
            if (this.author != null && this.author.getName() != null && !this.author.getName().isEmpty()) {
                this.batch.add(new Payload(this.batchIdNum + "_author_name", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + this.author.getName(), 5000));
            }
        }
        if (this.translateFields) {
            for (int i = 0; i < this.fields.size(); i++) {
                MessageEmbed.Field field = this.fields.get(i);
                this.batch.add(new Payload(this.batchIdNum + "_field_" + i + "_name", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + field.getName(), 5000));
                this.batch.add(new Payload(this.batchIdNum + "_field_" + i + "_value", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + field.getValue(), 5000));
            }
        }

        if (!this.batch.getPayloads().isEmpty()) {
            this.batch.queue(0);
            List<Payload> outPayloads = batch.getPayloads();

            Map<String, String> results = new HashMap<>();
            for (Payload p : outPayloads) {
                results.put(p.getId(), p.getResult());
            }

            if (this.translateTitle) {
                String title = (this.title != null && !this.title.isEmpty()) ? results.get(this.batchIdNum + "_title") : null;
                this.setTitle(title);
            }

            if (this.translateDescription) {
                String description = !this.description.isEmpty() ? results.get(this.batchIdNum + "_description") : "";
                this.setDescription(new StringBuilder(description));
            }

            if (this.translateFooter) {
                if (this.footer != null && this.footer.getText() != null && !this.footer.getText().isEmpty()) {
                    String footerText = !this.footer.getText().isEmpty() ? results.get(this.batchIdNum + "_footer_text") : null;
                    String footerIconUrl = (this.footer.getIconUrl() != null && !this.footer.getIconUrl().isEmpty()) ? this.footer.getIconUrl() : null;
                    this.setFooter(footerText, footerIconUrl);
                }
            }

            if (this.translateAuthor) {
                if (this.author != null && this.author.getName() != null && !this.author.getName().isEmpty()) {
                    String authorName = (this.author.getName() != null && !this.author.getName().isEmpty()) ? results.get(this.batchIdNum + "_author_name") : null;
                    String authorUrl = (this.author.getUrl() != null && !this.author.getUrl().isEmpty()) ? this.author.getUrl() : null;
                    String authorIconUrl = (this.author.getIconUrl() != null && !this.author.getIconUrl().isEmpty()) ? this.author.getIconUrl() : null;
                    this.setAuthor(authorName, authorUrl, authorIconUrl);
                }
            }

            if (this.translateFields) {
                Map<String, String> fieldResults = results.entrySet().stream().filter(e -> e.getKey().startsWith(this.batchIdNum + "_field_")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                int maxNum = fieldResults.keySet().stream().map(key -> key.split("_")).filter(parts -> parts.length > 2).mapToInt(parts -> Integer.parseInt(parts[2])).max().orElse(0);

                List<MessageEmbed.Field> fields = new ArrayList<>();

                for (int i = 0; i < maxNum; i++) {
                    String name = fieldResults.get(this.batchIdNum + "_field_" + i + "_name");
                    String value = fieldResults.get(this.batchIdNum + "_field_" + i + "_value");
                    fields.add(new MessageEmbed.Field(name, value, this.fields.get(i).isInline()));
                }

                this.clearFields();
                this.fields.addAll(fields);
            }
        }

        return this;
    }

    public CompletableFuture<TranslateEmbedBuilder> translateAsync(@NotNull String targetLanguage) {
        return CompletableFuture.supplyAsync(() -> translate(targetLanguage));
    }
}
