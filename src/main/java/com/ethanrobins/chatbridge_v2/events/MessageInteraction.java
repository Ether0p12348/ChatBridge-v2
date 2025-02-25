package com.ethanrobins.chatbridge_v2.events;

import com.ethanrobins.chatbridge_v2.*;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public class MessageInteraction extends ListenerAdapter {
    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent e) {
        super.onMessageContextInteraction(e);

        switch (e.getName()) {
            case "Secret Translation":
            case "Public Translation":
                boolean isPublic = e.getName().equals("Public Translation");
                e.deferReply().setEphemeral(!isPublic).queue();

                Object reply = translateMessage(e, e.getUserLocale().getLocale());

                if (reply != null) {
                    if (reply instanceof String) {
                        e.getHook().editOriginal((String) reply).queue();
                    } else {
                        EndUserError err = (EndUserError) reply;
                        if (isPublic) {
                            e.getHook().deleteOriginal().queue();
                            e.reply(err.getLocaleMessages().get(e.getUserLocale())).setEphemeral(true).queue();
                        } else {
                            e.getHook().editOriginal(err.getLocaleMessages().get(e.getUserLocale())).queue();
                        }
                    }
                }
                break;
            default:
                String msg = switch (e.getUserLocale()) {
                    case DiscordLocale.BULGARIAN -> "Това взаимодействие беше премахнато от разработчика";
                    case DiscordLocale.CHINESE_CHINA -> "此交互已被开发者移除";
                    case DiscordLocale.CHINESE_TAIWAN -> "此互動已被開發者移除";
                    case DiscordLocale.CROATIAN -> "Ovu interakciju je uklonio programer";
                    case DiscordLocale.CZECH -> "Tato interakce byla vývojářem odstraněna";
                    case DiscordLocale.DANISH -> "Denne interaktion er blevet fjernet af udvikleren";
                    case DiscordLocale.DUTCH -> "Deze interactie is door de ontwikkelaar verwijderd";
//                    case DiscordLocale.ENGLISH_UK -> "This interaction has been removed by the developer";
//                    case DiscordLocale.ENGLISH_US -> "This interaction has been removed by the developer";
                    case DiscordLocale.FINNISH -> "Tämä vuorovaikutus on poistettu kehittäjän toimesta";
                    case DiscordLocale.FRENCH -> "Cette interaction a été supprimée par le développeur";
                    case DiscordLocale.GERMAN -> "Diese Interaktion wurde vom Entwickler entfernt";
                    case DiscordLocale.GREEK -> "Αυτή η αλληλεπίδραση έχει αφαιρεθεί από τον προγραμματιστή";
                    case DiscordLocale.HINDI -> "इस इंटरैक्शन को डेवलपर द्वारा हटा दिया गया है";
                    case DiscordLocale.HUNGARIAN -> "Ezt az interakciót a fejlesztő eltávolította";
                    case DiscordLocale.INDONESIAN -> "Interaksi ini telah dihapus oleh pengembang";
                    case DiscordLocale.ITALIAN -> "Questa interazione è stata rimossa dallo sviluppatore";
                    case DiscordLocale.JAPANESE -> "このインタラクションは開発者によって削除されました";
                    case DiscordLocale.KOREAN -> "이 상호작용은 개발자에 의해 제거되었습니다";
                    case DiscordLocale.LITHUANIAN -> "Šią sąveiką pašalino kūrėjas";
                    case DiscordLocale.NORWEGIAN -> "Denne interaksjonen har blitt fjernet av utvikleren";
                    case DiscordLocale.POLISH -> "Ta interakcja została usunięta przez programistę";
                    case DiscordLocale.PORTUGUESE_BRAZILIAN -> "Esta interação foi removida pelo desenvolvedor";
                    case DiscordLocale.ROMANIAN_ROMANIA -> "Această interacțiune a fost eliminată de către dezvoltator";
                    case DiscordLocale.RUSSIAN -> "Это взаимодействие было удалено разработчиком";
                    case DiscordLocale.SPANISH -> "Esta interacción ha sido eliminada por el desarrollador";
                    case DiscordLocale.SPANISH_LATAM -> "Esta interacción ha sido eliminada por el desarrollador";
                    case DiscordLocale.SWEDISH -> "Den här interaktionen har tagits bort av utvecklaren";
                    case DiscordLocale.THAI -> "การโต้ตอบนี้ถูกลบโดยนักพัฒนา";
                    case DiscordLocale.TURKISH -> "Bu etkileşim geliştirici tarafından kaldırıldı";
                    case DiscordLocale.UKRAINIAN -> "Цю взаємодію було видалено розробником";
                    case DiscordLocale.VIETNAMESE -> "Tương tác này đã bị nhà phát triển gỡ bỏ";
                    default -> "This interaction has been removed by the developer";
                };

                e.reply(msg).setEphemeral(true).queue();
                break;
        }
    }

    public Object translateMessage (@NotNull MessageContextInteractionEvent event, @NotNull String targetLanguage) {
        List<MessageEmbed> embeds = new ArrayList<>(event.getTarget().getEmbeds());
        embeds.removeIf(embed -> embed.getType() != EmbedType.RICH);

        if (embeds.isEmpty()) {
            Payload payload = new Payload(null, TranslateType.DECORATED.getSystemPrompt(), "(" + targetLanguage + ")" + event.getTarget().getContentRaw(), 5000);
            try {
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                CompletableFuture<String> messageFuture = new CompletableFuture<>();

                scheduler.schedule(() -> {
                    payload.translateAsync().whenComplete((result, ex) -> {
                        if (ex != null) {
                            messageFuture.completeExceptionally(ex);
                        } else {
                            messageFuture.complete(result);
                        }
                    });
                }, 0, TimeUnit.MILLISECONDS);
                String message = event.getTarget().getJumpUrl() + ": " + payload.getResult();
                scheduler.shutdown();

                return message;
            } catch (Exception e) {
                e.printStackTrace();
                return buildEndUserError(e);
            }
        } else {
            String mainMessage = null;
            List<MessageEmbed> translatedEmbeds = new ArrayList<>();

            boolean hasMainContent = event.getTarget().getContentRaw() != null && !event.getTarget().getContentRaw().isEmpty();
            if (hasMainContent) {
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                CompletableFuture<String> mainMessageFuture = new CompletableFuture<>();
                Payload mainMessagePayload = new Payload(null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + event.getTarget().getContentRaw(), 5000);

                try {
                    scheduler.schedule(() -> {
                        mainMessagePayload.translateAsync().whenComplete((result, ex) -> {
                            if (ex != null) {
                                mainMessageFuture.completeExceptionally(ex);
                            } else {
                                mainMessageFuture.complete(result);
                            }
                        });
                    }, 0, TimeUnit.MILLISECONDS);

                    mainMessage = mainMessagePayload.getResult();
                    scheduler.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                    return buildEndUserError(e);
                }
            }

            for (MessageEmbed e : embeds) {
                TranslateEmbedBuilder builder = new TranslateEmbedBuilder(e).translate(targetLanguage);
                translatedEmbeds.add(builder.build());
            }

            return new MessageCreateBuilder().setContent(mainMessage).setEmbeds(translatedEmbeds).build();
//
//            Map<Integer, MessageEmbed> inOrderEmbeds = new HashMap<>();
//            int i = 0;
//            for (MessageEmbed embed : embeds) {
//                inOrderEmbeds.put(i, embed);
//
//                if (embed.getTitle() != null && !embed.getTitle().isEmpty()) {
//                    payloads.add(new Payload(batchIdNum + "_embed_" + i + "_title", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + embed.getTitle(), 5000));
//                }
//                if (embed.getDescription() != null && !embed.getDescription().isEmpty()) {
//                    payloads.add(new Payload(batchIdNum + "_embed_" + i + "_description", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + embed.getDescription(), 5000));
//                }
//                if (embed.getAuthor() != null) {
//                    if (embed.getAuthor().getName() != null && !embed.getAuthor().getName().isEmpty()) {
//                        payloads.add(new Payload(batchIdNum + "_embed_" + i + "_author_name", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + embed.getAuthor().getName(), 5000));
//                    }
//                }
//                if (embed.getFooter() != null) {
//                    if (embed.getFooter().getText() != null && !embed.getFooter().getText().isEmpty()) {
//                        payloads.add(new Payload(batchIdNum + "_embed_" + i + "_footer_text", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + embed.getFooter().getText(), 5000));
//                    }
//                }
//                if (!embed.getFields().isEmpty()) {
//                    int fi = 0;
//                    for (MessageEmbed.Field field : embed.getFields()) {
//                        if (field.getName() != null && !field.getName().isEmpty()) {
//                            payloads.add(new Payload(batchIdNum + "_embed_" + i + "_field_" + fi + "_name", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + field.getName(), 5000));
//                        }
//                        if (field.getValue() != null && !field.getValue().isEmpty()) {
//                            payloads.add(new Payload(batchIdNum + "_embed_" + i + "_field_" + fi + "_value", null, TranslateType.PLAIN.getSystemPrompt(), "(" + targetLanguage + ")" + field.getValue(), 5000));
//                        }
//
//                        fi++;
//                    }
//                }
//
//                i++;
//            }
//
//            Batch batch = new Batch(batchId, payloads);
//            try {
//                batch.queue(0);
//
//                List<Payload> outPayloads = batch.getPayloads();
//                Map<String, String> results = new HashMap<>();
//
//                for (Payload p : outPayloads) {
//                    results.put(p.getId(), p.getResult());
//                }
//
//                String message = results.get(batchIdNum + "_message") != null ? results.get(batchIdNum + "_message") : null;
//                results.remove(batchIdNum + "_message");
//
//                List<EmbedBuilder> embedBuilders = new ArrayList<>();
//                int maxNum = results.keySet().stream().map(key -> key.split("_")).filter(parts -> parts.length > 2).mapToInt(parts -> Integer.parseInt(parts[2])).max().orElse(0);
//
//                for (int n = 0; n < maxNum; n++) {
//                    final int nn = n;
//                    Map<String, String> embed = results.entrySet().stream().filter(e -> e.getKey().startsWith(batchIdNum + "_embed_" + nn)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//                    EmbedBuilder eb = new EmbedBuilder();
//
//                    eb.setColor(inOrderEmbeds.get(nn).getColor());
//
//                    if (embed.get(batchIdNum + "_embed_" + nn + "_title") != null) {
//                        eb.setTitle(embed.get(batchIdNum + "_embed_" + nn + "_title"));
//                    }
//
//                    if (inOrderEmbeds.get(nn).getUrl() != null) {
//                        eb.setUrl(inOrderEmbeds.get(nn).getUrl());
//                    }
//
//                    if (embed.get(batchIdNum + "_embed_" + nn + "_description") != null) {
//                        eb.setDescription(embed.get(batchIdNum + "_embed_" + nn + "_description"));
//                    }
//
//                    String authorUrl = null;
//                    String authorIcon = null;
//                    if (inOrderEmbeds.get(nn).getAuthor() != null) {
//                        authorUrl = Objects.requireNonNull(inOrderEmbeds.get(nn).getAuthor()).getUrl();
//                        authorIcon = Objects.requireNonNull(inOrderEmbeds.get(nn).getAuthor()).getIconUrl();
//                    }
//                    if (embed.get(batchIdNum + "_embed_" + nn + "_author_name") != null || authorUrl != null || authorIcon != null) {
//                        eb.setAuthor(embed.get(batchIdNum + "_embed_" + nn + "_author_name"), authorUrl, authorIcon);
//                    }
//
//                    String footerIcon = null;
//                    if (inOrderEmbeds.get(nn).getFooter() != null) {
//                        footerIcon = Objects.requireNonNull(inOrderEmbeds.get(nn).getFooter()).getIconUrl();
//                    }
//                    if (embed.get(batchIdNum + "_embed_" + nn + "_footer_text") != null || footerIcon != null) {
//                        eb.setFooter(embed.get(batchIdNum + "_embed_" + nn + "_footer_text"), footerIcon);
//                    }
//
//                    if (inOrderEmbeds.get(nn).getImage() != null) {
//                        eb.setImage(Objects.requireNonNull(inOrderEmbeds.get(nn).getImage()).getUrl());
//                    }
//
//                    if (inOrderEmbeds.get(nn).getThumbnail() != null) {
//                        eb.setThumbnail(Objects.requireNonNull(inOrderEmbeds.get(nn).getThumbnail()).getUrl());
//                    }
//
//                    TemporalAccessor timestamp = null;
//                    if (inOrderEmbeds.get(nn).getTimestamp() != null) {
//                        timestamp = Objects.requireNonNull(inOrderEmbeds.get(nn).getTimestamp());
//                    }
//                    if (timestamp != null) {
//                        eb.setTimestamp(timestamp);
//                    }
//
//                    for (MessageEmbed.Field field : ) {
//
//                    }
//                    // may have to make a class for all of this
//                    embedBuilders.add(eb);
//                }

//                int in = 0;
//                String mainContent = batch.get(batchIdNum + "_message") != null ? batch.get(batchIdNum + "_message").getResult() : null;
//                String title = batch.get(batchIdNum + "_embed_" + in + "_title") != null ? batch.get(batchIdNum + "_embed_" + in + "_title").getResult() : null;
//                String description = batch.get(batchIdNum + "_embed_" + in + "_description") != null ? batch.get(batchIdNum + "_embed_" + in + "_description").getResult() : null;
//                String authorName = batch.get(batchIdNum + "_embed_" + in + "_author_name") != null ? batch.get(batchIdNum + "_embed_" + in + "_author_name").getResult() : null;
//                String footerText = batch.get(batchIdNum + "_embed_" + in + "_footer_text") != null ? batch.get(batchIdNum + "_embed_" + in + "_footer_text").getResult() : null;
//                List<String> fields = new ArrayList<>();
//                List<Payload> fieldPayloads = batch.getPayloads()
//                        .stream().filter(p -> p.getId().startsWith("") && p.getId().contains("_field_")).toList()
//                        .stream().sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getId().substring(p.getId().lastIndexOf("_") - 1)))).toList();
//                if (!fieldPayloads.isEmpty()) {
//                    for (Payload p : fieldPayloads) {
//                        fields.add(p.getResult());
//                    }
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                return buildEndUserError(ex);
//            }
        }
    }

    private EndUserError buildEndUserError(Exception ex) {
        Map<DiscordLocale, String> localeMessages = new HashMap<>();

        String discordInvite = ChatBridge.getSecret().get("discord", "discordInvite");
        localeMessages.put(DiscordLocale.BULGARIAN, "Неуспех при превеждането на съобщението. Моля, докладвайте този инцидент на официалния сървър в Discord за ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.CHINESE_CHINA, "消息翻译失败。请将此事件报告至官方的 ChatBridge Discord 服务器：" + discordInvite);
        localeMessages.put(DiscordLocale.CHINESE_TAIWAN, "訊息翻譯失敗。請將此事件回報至官方的 ChatBridge Discord 伺服器：" + discordInvite);
        localeMessages.put(DiscordLocale.CROATIAN, "Neuspjelo prevođenje poruke. Molimo prijavite ovaj incident na službenom ChatBridge Discord poslužitelju: " + discordInvite);
        localeMessages.put(DiscordLocale.CZECH, "Nepodařilo se přeložit zprávu. Nahlaste tento incident na oficiálním Discord serveru ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.DANISH, "Kunne ikke oversætte beskeden. Rapporter venligst denne hændelse til den officielle ChatBridge Discord-server: " + discordInvite);
        localeMessages.put(DiscordLocale.DUTCH, "Het vertalen van het bericht is mislukt. Meld dit voorval alstublieft op de officiële ChatBridge Discord Server: " + discordInvite);
        localeMessages.put(DiscordLocale.ENGLISH_UK, "Failed to translate the message. Please report this incident to the official ChatBridge Discord Server: " + discordInvite);
        localeMessages.put(DiscordLocale.ENGLISH_US, "Failed to translate the message. Please report this incident to the official ChatBridge Discord Server: " + discordInvite);
        localeMessages.put(DiscordLocale.FINNISH, "Viestin kääntäminen epäonnistui. Ilmoita tästä tapauksesta viralliselle ChatBridge Discord-palvelimelle: " + discordInvite);
        localeMessages.put(DiscordLocale.FRENCH, "Échec de la traduction du message. Veuillez signaler cet incident au serveur Discord officiel de ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.GERMAN, "Die Nachricht konnte nicht übersetzt werden. Bitte melden Sie diesen Vorfall dem offiziellen ChatBridge Discord-Server: " + discordInvite);
        localeMessages.put(DiscordLocale.GREEK, "Αποτυχία μετάφρασης του μηνύματος. Παρακαλώ αναφέρετε αυτό το περιστατικό στον επίσημο διακομιστή Discord του ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.HINDI, "संदेश का अनुवाद करने में विफल। कृपया इस घटना की रिपोर्ट आधिकारिक ChatBridge Discord सर्वर पर करें: " + discordInvite);
        localeMessages.put(DiscordLocale.HUNGARIAN, "Nem sikerült lefordítani az üzenetet. Kérjük, jelentse ezt az esetet a hivatalos ChatBridge Discord szerveren: " + discordInvite);
        localeMessages.put(DiscordLocale.INDONESIAN, "Gagal menerjemahkan pesan. Harap laporkan insiden ini ke Server Discord resmi ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.ITALIAN, "Impossibile tradurre il messaggio. Si prega di segnalare questo incidente al server ufficiale di ChatBridge su Discord: " + discordInvite);
        localeMessages.put(DiscordLocale.JAPANESE, "メッセージの翻訳に失敗しました。このインシデントを公式 ChatBridge Discord サーバーに報告してください: " + discordInvite);
        localeMessages.put(DiscordLocale.KOREAN, "메시지 번역에 실패했습니다. 이 사건을 공식 ChatBridge Discord 서버에 보고해 주세요: " + discordInvite);
        localeMessages.put(DiscordLocale.LITHUANIAN, "Nepavyko išversti žinutės. Prašome pranešti apie šį incidentą oficialiame ChatBridge Discord serveryje: " + discordInvite);
        localeMessages.put(DiscordLocale.NORWEGIAN, "Kunne ikke oversette meldingen. Vennligst rapporter denne hendelsen til den offisielle ChatBridge Discord-serveren: " + discordInvite);
        localeMessages.put(DiscordLocale.POLISH, "Nie udało się przetłumaczyć wiadomości. Proszę zgłosić ten incydent na oficjalnym serwerze Discord ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.PORTUGUESE_BRAZILIAN, "Falha ao traduzir a mensagem. Por favor, reporte este incidente ao servidor oficial do ChatBridge no Discord: " + discordInvite);
        localeMessages.put(DiscordLocale.ROMANIAN_ROMANIA, "Nu s-a reușit traducerea mesajului. Vă rugăm să raportați acest incident pe serverul oficial ChatBridge Discord: " + discordInvite);
        localeMessages.put(DiscordLocale.RUSSIAN, "Не удалось перевести сообщение. Пожалуйста, сообщите об этом инциденте на официальном сервере ChatBridge в Discord: " + discordInvite);
        localeMessages.put(DiscordLocale.SPANISH, "No se pudo traducir el mensaje. Informe de este incidente en el servidor oficial de Discord de ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.SPANISH_LATAM, "No se pudo traducir el mensaje. Por favor, informe de este incidente en el servidor de Discord oficial de ChatBridge: " + discordInvite);
        localeMessages.put(DiscordLocale.SWEDISH, "Misslyckades med att översätta meddelandet. Vänligen rapportera denna incident till den officiella ChatBridge Discord-servern: " + discordInvite);
        localeMessages.put(DiscordLocale.THAI, "ไม่สามารถแปลข้อความนี้ได้ โปรดแจ้งเหตุการณ์นี้ให้เซิร์ฟเวอร์ Discord อย่างเป็นทางการของ ChatBridge ทราบ: " + discordInvite);
        localeMessages.put(DiscordLocale.TURKISH, "Mesaj tercüme edilemedi. Lütfen bu olayı resmi ChatBridge Discord Sunucusuna bildirin: " + discordInvite);
        localeMessages.put(DiscordLocale.UKRAINIAN, "Не вдалося перекласти повідомлення. Будь ласка, повідомте про цей випадок на офіційному сервері ChatBridge у Discord: " + discordInvite);
        localeMessages.put(DiscordLocale.VIETNAMESE, "Không thể dịch tin nhắn. Vui lòng báo cáo sự cố này cho Máy chủ Discord chính thức của ChatBridge: " + discordInvite);

        return new EndUserError(ex, localeMessages);
    }
}
