package com.ethanrobins.chatbridge_v2.events;

import com.ethanrobins.chatbridge_v2.*;
import com.ethanrobins.chatbridge_v2.drivers.*;
import com.ethanrobins.chatbridge_v2.exceptions.EndUserError;
import com.ethanrobins.chatbridge_v2.utils.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// TODO: Add translations to link embeds
public class MessageInteraction extends ListenerAdapter {
    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent e) {
        super.onMessageContextInteraction(e);

        // save the user in database with their locale
        CompletableFuture.runAsync(() -> {
            MySQL mysql = null;
            try {
                mysql = new MySQL();
                MySQL.Status status = mysql.updateLocale(e.getUser().getId(), e.getUserLocale());

                User user = ChatBridge.getJda().retrieveUserById(e.getUser().getId()).complete();
                if (user != null) {
                    System.out.println(status);
                    if (status.isInserted()) {
                        user.openPrivateChannel().queue(channel -> {
                            try {
                                channel.sendMessageEmbeds(Messages.firstInteraction(e.getUserLocale())).queue();
                            } catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        });

                        // check caches
                        for (MessageReceivedEvent mre : Messages.getUnregisteredMessages(e.getUser().getId())) {
                            Messages.removeUnregisteredMessage(mre);
                            MessageReceived.translateMessageAsync(mre, e.getUserLocale());
                        }
                    }
                }

                mysql.close();
            } catch (SQLException | RuntimeException ex) {
                if (mysql != null && !mysql.getStatus().isClosed()) {
                    mysql.close();
                }
                ex.printStackTrace();
            }
        });


        switch (e.getName()) {
            case "priv-translate-dev":
            case "pub-translate-dev":
            case "priv-translate":
            case "pub-translate":
                boolean isPublic = e.getName().equals("Public Translation") || e.getName().equals("Public Translation (dev)");
                Member member = e.getMember();
                if (member != null && isPublic && e.getGuild() != null && !e.getMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_SEND)) {
                    isPublic = false;
                }

                e.deferReply().setEphemeral(!isPublic).queue();

                translateMessageAsync(e, isPublic);

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

    public void translateMessageAsync(@NotNull MessageContextInteractionEvent event, boolean isPublic) {
        final String targetLocale = event.getUserLocale().getLocale();

        CompletableFuture.runAsync(() -> {
            try {
                List<MessageEmbed> embeds = new ArrayList<>(event.getTarget().getEmbeds());
                embeds.removeIf(embed -> embed.getType() != EmbedType.RICH);

                if (embeds.isEmpty()) {
                    Request request = new Request(new Request.Prompt(null, null, event.getUserLocale().getLocale(), event.getTarget().getContentRaw()));
                    request.queue().thenAccept(response -> {
                        Response.Data responseData = response.getOutput().getContent().getData();
                        String reply = event.getTarget().getJumpUrl() + ": **(" + responseData.getSource().getTag() + ") " + responseData.getSource().getLang() + " → (" + responseData.getTarget().getTag() + ") " + responseData.getTarget().getLang() + "**\n" +
                                responseData.getTarget().getSafe();
                        event.getHook().editOriginal(reply).queue();
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        EndUserError err = buildEndUserError((Exception) ex);
                        event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
                        return null;
                    });
//                    Payload payload = new Payload(null, TranslateType.DECORATED.getSystemPrompt(), Payload.userMessage(targetLocale, event.getTarget().getContentRaw()), 5000);
//                    payload.translateAsync().thenAccept(result -> {
//                        String reply = event.getTarget().getJumpUrl() + ": " + result;
//                        event.getHook().editOriginal(reply).queue();
//                    }).exceptionally(ex -> {
//                        ex.printStackTrace();
//                        EndUserError err = buildEndUserError((Exception) ex);
//                        event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
//                        return null;
//                    });
                } else {
                    event.getHook().setEphemeral(true).editOriginal("Embeds are not supported at this time.").queue();
//                    CompletableFuture<String> mainMessageFuture = CompletableFuture.completedFuture(null);
//                    event.getTarget().getContentRaw();
//                    if (event.getTarget().getContentRaw() != null && !event.getTarget().getContentRaw().isEmpty()) {
//                        Payload mainMessagePayload = new Payload(null, TranslateType.DECORATED.getSystemPrompt(), Payload.userMessage(targetLocale, event.getTarget().getContentRaw()), 5000);
//                        mainMessageFuture = mainMessagePayload.translateAsync();
//                    } else {
//                        Payload mainMessagePayload = new Payload(null, TranslateType.CAPTION.getSystemPrompt(), Payload.userMessage(targetLocale, event.getTarget().getContentRaw()), 5000);
//                        mainMessageFuture = mainMessagePayload.translateAsync();
//                    }
//
//                    List<CompletableFuture<TranslateEmbedBuilder>> embedFutures = new ArrayList<>();
//                    for (MessageEmbed e : embeds) {
//                        embedFutures.add(new TranslateEmbedBuilder(e).translateAsync(targetLocale));
//                    }
//
//                    mainMessageFuture.thenCombine(
//                            CompletableFuture.allOf(embedFutures.toArray(new CompletableFuture[0]))
//                                    .thenApply(v -> embedFutures.stream()
//                                            .map(CompletableFuture::join)
//                                            .map(TranslateEmbedBuilder::build)
//                                            .toList()),
//                            (mainMessage, builtEmbeds) -> MessageEditData.fromCreateData(new MessageCreateBuilder().setContent(mainMessage).setEmbeds(builtEmbeds).build())
//                    ).thenAccept(finalMessage -> event.getHook().editOriginal(finalMessage).queue())
//                            .exceptionally(ex -> {
//                        ex.printStackTrace();
//                        EndUserError err = buildEndUserError((Exception) ex);
//                        event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
//                        return null;
//                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                EndUserError err = buildEndUserError(ex);
                event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
            }
        });
    }

    public static EndUserError buildEndUserError(Exception ex) {
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
