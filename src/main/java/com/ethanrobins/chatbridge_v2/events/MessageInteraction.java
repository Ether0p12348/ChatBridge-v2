package com.ethanrobins.chatbridge_v2.events;

import com.ethanrobins.chatbridge_v2.*;
import com.ethanrobins.chatbridge_v2.drivers.*;
import com.ethanrobins.chatbridge_v2.exceptions.EndUserError;
import com.ethanrobins.chatbridge_v2.utils.Messages;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
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
            case "report":
                e.deferReply().setEphemeral(true).queue();

                if (e.getTarget().getAuthor().isBot() && e.getTarget().getAuthor().getId().equals(ChatBridge.getSecret().get("discord", "userId"))) {
                    e.getHook().editOriginal("This function has not been implemented yet!").queue();
                } else {
                    String msg = switch (e.getUserLocale()) {
                        case DiscordLocale.BULGARIAN -> "Това съобщение не може да бъде докладвано — не е изпратено от ChatBridge. Изберете отговор от ChatBridge и опитайте отново.";
                        case DiscordLocale.CHINESE_CHINA -> "无法举报此消息 — 它不是由 ChatBridge 发送的。请选择一条 ChatBridge 的回复，然后重试。";
                        case DiscordLocale.CHINESE_TAIWAN -> "無法檢舉此訊息 — 這不是由 ChatBridge 發送的。請選擇一則 ChatBridge 的回覆，然後再試一次。";
                        case DiscordLocale.CROATIAN -> "Ne možete prijaviti ovu poruku — nije je poslao ChatBridge. Odaberite odgovor ChatBridgea i pokušajte ponovno.";
                        case DiscordLocale.CZECH -> "Tuto zprávu nelze nahlásit — neposlal ji ChatBridge. Vyberte odpověď od ChatBridgeu a zkuste to znovu.";
                        case DiscordLocale.DANISH -> "Du kan ikke anmelde denne besked — den er ikke sendt af ChatBridge. Vælg et svar fra ChatBridge, og prøv igen.";
                        case DiscordLocale.DUTCH -> "Je kunt dit bericht niet melden — het is niet door ChatBridge verzonden. Selecteer een ChatBridge-reactie en probeer het opnieuw.";
//                        case DiscordLocale.ENGLISH_UK -> "Can’t report this message — it wasn’t sent by ChatBridge. Select a ChatBridge reply and try again.";
//                        case DiscordLocale.ENGLISH_US -> "Can’t report this message — it wasn’t sent by ChatBridge. Select a ChatBridge reply and try again.";
                        case DiscordLocale.FINNISH -> "Tätä viestiä ei voi ilmoittaa — ChatBridge ei lähettänyt sitä. Valitse ChatBridge-vastaus ja yritä uudelleen.";
                        case DiscordLocale.FRENCH -> "Impossible de signaler ce message — il n’a pas été envoyé par ChatBridge. Sélectionnez une réponse de ChatBridge et réessayez.";
                        case DiscordLocale.GERMAN -> "Diese Nachricht kann nicht gemeldet werden — sie wurde nicht von ChatBridge gesendet. Wähle eine ChatBridge-Antwort und versuche es erneut.";
                        case DiscordLocale.GREEK -> "Δεν γίνεται να αναφέρεις αυτό το μήνυμα — δεν στάλθηκε από το ChatBridge. Επίλεξε μια απάντηση του ChatBridge και δοκίμασε ξανά.";
                        case DiscordLocale.HINDI -> "इस संदेश की रिपोर्ट नहीं की जा सकती — यह ChatBridge द्वारा नहीं भेजा गया था। कृपया ChatBridge का कोई जवाब चुनें और फिर से प्रयास करें.";
                        case DiscordLocale.HUNGARIAN -> "Ezt az üzenetet nem lehet jelenteni — nem a ChatBridge küldte. Válassz egy ChatBridge-választ, és próbáld újra.";
                        case DiscordLocale.INDONESIAN -> "Tidak dapat melaporkan pesan ini — pesan ini tidak dikirim oleh ChatBridge. Pilih balasan dari ChatBridge lalu coba lagi.";
                        case DiscordLocale.ITALIAN -> "Impossibile segnalare questo messaggio — non è stato inviato da ChatBridge. Seleziona una risposta di ChatBridge e riprova.";
                        case DiscordLocale.JAPANESE -> "このメッセージは報告できません — ChatBridge から送信されたものではありません。ChatBridge の返信を選んで、もう一度お試しください。";
                        case DiscordLocale.KOREAN -> "이 메시지는 신고할 수 없습니다 — ChatBridge에서 보낸 것이 아닙니다. ChatBridge의 답장을 선택하고 다시 시도하세요.";
                        case DiscordLocale.LITHUANIAN -> "Negalite pranešti apie šią žinutę — ją neišsiuntė „ChatBridge“. Pasirinkite „ChatBridge“ atsakymą ir bandykite dar kartą.";
                        case DiscordLocale.NORWEGIAN -> "Du kan ikke rapportere denne meldingen — den ble ikke sendt av ChatBridge. Velg et ChatBridge-svar og prøv igjen.";
                        case DiscordLocale.POLISH -> "Nie można zgłosić tej wiadomości — nie została wysłana przez ChatBridge. Wybierz odpowiedź od ChatBridge i spróbuj ponownie.";
                        case DiscordLocale.PORTUGUESE_BRAZILIAN -> "Não é possível denunciar esta mensagem — ela não foi enviada pelo ChatBridge. Selecione uma resposta do ChatBridge e tente novamente.";
                        case DiscordLocale.ROMANIAN_ROMANIA -> "Nu poți raporta acest mesaj — nu a fost trimis de ChatBridge. Selectează un răspuns de la ChatBridge și încearcă din nou.";
                        case DiscordLocale.RUSSIAN -> "Вы не можете пожаловаться на это сообщение — его не отправлял ChatBridge. Выберите ответ от ChatBridge и попробуйте ещё раз.";
                        case DiscordLocale.SPANISH -> "No puedes denunciar este mensaje — no fue enviado por ChatBridge. Selecciona una respuesta de ChatBridge y vuelve a intentarlo.";
                        case DiscordLocale.SPANISH_LATAM -> "No puedes reportar este mensaje — no fue enviado por ChatBridge. Selecciona una respuesta de ChatBridge y vuelve a intentarlo.";
                        case DiscordLocale.SWEDISH -> "Du kan inte anmäla det här meddelandet — det skickades inte av ChatBridge. Välj ett svar från ChatBridge och försök igen.";
                        case DiscordLocale.THAI -> "ไม่สามารถรายงานข้อความนี้ได้ — ข้อความนี้ไม่ได้ส่งโดย ChatBridge โปรดเลือกการตอบกลับของ ChatBridge แล้วลองอีกครั้ง";
                        case DiscordLocale.TURKISH -> "Bu mesaj bildirilemez — ChatBridge tarafından gönderilmedi. Bir ChatBridge yanıtı seçip tekrar deneyin.";
                        case DiscordLocale.UKRAINIAN -> "Не можна поскаржитися на це повідомлення — його не надсилав ChatBridge. Виберіть відповідь від ChatBridge і спробуйте ще раз.";
                        case DiscordLocale.VIETNAMESE -> "Không thể báo cáo tin nhắn này — tin nhắn không do ChatBridge gửi. Hãy chọn một phản hồi của ChatBridge rồi thử lại.";
                        default -> "Can’t report this message — it wasn’t sent by ChatBridge. Select a ChatBridge reply and try again.";
                    };

                    e.getHook().editOriginal(msg).queue();
                }
                break;
            case "priv-translate-dev":
            case "pub-translate-dev":
            case "priv-translate":
            case "pub-translate":
                boolean isPublic = e.getName().equals("pub-translate") || e.getName().equals("pub-translate-dev");
                Member member = e.getMember();
                if (member != null && isPublic && e.getGuild() != null && !e.getMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_SEND)) {
                    isPublic = false;
                }

                e.deferReply().setEphemeral(!isPublic).queue();

                translateMessageAsync(e);

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

    public void translateMessageAsync(@NotNull MessageContextInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                List<MessageEmbed> embeds = new ArrayList<>(event.getTarget().getEmbeds());
                embeds.removeIf(embed -> embed.getType() != EmbedType.RICH);

                if (embeds.isEmpty()) {
                    final ReplyGroup rg = new ReplyGroup(event);

                    Request request = new Request(new Request.Prompt(PromptType.MESSAGE.getId(), PromptType.MESSAGE.getVersion(), event.getUserLocale().getLocale(), event.getTarget().getContentRaw()));
                    request.queue().thenAccept(response -> {
                        Response.Data responseData = Objects.requireNonNull(Objects.requireNonNull(response.getOutput()).getContent()).getData();

                        if (responseData.getTarget() instanceof Response.Data.MessageTarget tgt) {
                            Response.Data.Source src = responseData.getSource();
                            rg.setMessage(getCaption(event.getTarget().getJumpUrl(), src, tgt, tgt.getExplicit()));
                        } else {
                            System.err.println("Unexpected target type: " + responseData.getTarget().getClass().getName());
                        }
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        EndUserError err = buildEndUserError((Exception) ex);
                        event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
                        return null;
                    });
                } else {
                    final ReplyGroup rg = new ReplyGroup(event, embeds.size());
                    boolean isFirst = true;
                    for (MessageEmbed origEmbed : embeds) {
                        Request request = new Request(new Request.Prompt(PromptType.EMBED.getId(), PromptType.EMBED.getVersion(), event.getUserLocale().getLocale(), isFirst ? event.getTarget().getContentRaw() : null, origEmbed.getTitle(), origEmbed.getAuthor(), origEmbed.getDescription(), origEmbed.getFooter(), origEmbed.getFields()));
                        isFirst = false;
                        request.queue().thenAccept(response -> {
                            EmbedBuilder e = new EmbedBuilder();
                            e.copyFrom(origEmbed);
                            Response.Data responseData = Objects.requireNonNull(Objects.requireNonNull(response.getOutput()).getContent()).getData();
                            if (responseData.getTarget() instanceof Response.Data.EmbedTarget tgt) {
                                Response.Data.Source src = responseData.getSource();
                                e.setTitle(tgt.getExplicit().getTitle());
                                e.setAuthor(tgt.getExplicit().getAuthor(), origEmbed.getAuthor() != null ? origEmbed.getAuthor().getUrl() : null, origEmbed.getAuthor() != null ? origEmbed.getAuthor().getIconUrl() : null);
                                e.setDescription(tgt.getExplicit().getDescription());
                                e.setFooter(tgt.getExplicit().getFooter(), origEmbed.getFooter() != null ? origEmbed.getFooter().getIconUrl() : null);
                                e.clearFields();
                                if (tgt.getExplicit().getFields() != null && !tgt.getExplicit().getFields().isEmpty()) {
                                    for (int i = 0; i < tgt.getExplicit().getFields().size(); i++) {
                                        Response.Data.EmbedContent.Field f = tgt.getExplicit().getFields().get(i);
                                        if (f == null) continue;
                                        String name = f.getName();
                                        if (name == null) name = "";
                                        String value = f.getValue();
                                        if (value == null) value = "";

                                        e.addField(name, value, origEmbed.getFields().get(i).isInline());
                                    }
                                }

                                if (tgt.getExplicit().getMessage() != null) {
                                    rg.setMessage(getCaption(event.getTarget().getJumpUrl(), src, tgt, tgt.getExplicit().getMessage()));
                                }
                                rg.addEmbed(e.build());
                            } else {
                                System.err.println("Unexpected target type: " + responseData.getTarget().getClass().getName());
                            }
                        }).exceptionally(ex -> {
                            ex.printStackTrace();
                            EndUserError err = buildEndUserError((Exception) ex);
                            event.getHook().setEphemeral(true).editOriginal(err.getLocaleMessages().get(event.getUserLocale())).queue();
                            return null;
                        });
                    }
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

    public static String getCaption(String jumpUrl, Response.Data.Source src, Response.Data.Target<?> tgt, String msg) {
        return "%jumpUrl%: **(%srcTag%) %srcLang% → (%tgtTag%) %tgtLang%**\n%message%"
                .replace("%jumpUrl%", jumpUrl)
                .replace("%srcTag%", src.getTag())
                .replace("%srcLang%", src.getLang())
                .replace("%tgtTag%", tgt.getTag())
                .replace("%tgtLang%", tgt.getLang())
                .replace("%message%", msg);
    }

    @Getter
    public static class ReplyGroup {
        private @NotNull final MessageContextInteractionEvent event;
        private @Nullable String message = null;
        private final @NotNull List<MessageEmbed> embeds = new ArrayList<>();

        private final boolean messageOnly;
        private final int waitCount;
        private int finishCount = 0;

        public ReplyGroup(@NotNull MessageContextInteractionEvent event, int waitCount) {
            this.event = event;
            this.messageOnly = false;
            if (waitCount > 0) {
                this.waitCount = waitCount;
            } else {
                throw new IllegalArgumentException("waitCount must be greater than 0");
            }
        }
        public ReplyGroup(@NotNull MessageContextInteractionEvent event) {
            this.event = event;
            this.messageOnly = true;
            this.waitCount = 0;
        }

        public void setMessage(String message) {
            this.message = message;
            if (this.messageOnly) {
                complete();
            }
        }

        public void addEmbed(MessageEmbed embed) {
            this.embeds.add(embed);

            finishCount++;
            if (this.waitCount != 0 && finishCount >= waitCount) {
                complete();
            }
        }

        public void complete() {
            InteractionHook hook = this.event.getHook();
            if (this.message != null) hook.editOriginal(this.message).queue();
            if (!this.embeds.isEmpty()) hook.editOriginalEmbeds(this.embeds).queue();
        }
    }
}
