package com.ethanrobins.chatbridge_v2.events;

import com.ethanrobins.chatbridge_v2.drivers.MySQL;
import com.ethanrobins.chatbridge_v2.drivers.Payload;
import com.ethanrobins.chatbridge_v2.drivers.TranslateType;
import com.ethanrobins.chatbridge_v2.exceptions.EndUserError;
import com.ethanrobins.chatbridge_v2.utils.Messages;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MessageReceived extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        super.onMessageReceived(e);

        if (!e.getAuthor().isBot() && e.getChannelType() == ChannelType.PRIVATE) {
            try {
                MySQL mysql = new MySQL();
                DiscordLocale locale = mysql.getLocale(e.getAuthor().getId(), true);
                System.out.println(locale);
                mysql.close();
                if (locale == null) {
                    MessageEmbed embed = Messages.firstPrivateMessageUnregistered(e);

                    e.getChannel().sendMessageEmbeds(embed).queue();
                } else {

                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void translateMessageAsync(@NotNull MessageReceivedEvent event, @NotNull DiscordLocale locale) {
        String targetLocale = locale.getLocale();

        String loadingMsg = switch (locale) {
            case DiscordLocale.BULGARIAN -> "Съобщението се превежда. Моля, изчакайте...";
            case DiscordLocale.CHINESE_CHINA, DiscordLocale.CHINESE_TAIWAN -> "消息正在翻译中。请稍候...";
            case DiscordLocale.CROATIAN -> "Poruka se prevodi. Molimo pričekajte...";
            case DiscordLocale.CZECH -> "Zpráva se překládá. Prosím, čekejte...";
            case DiscordLocale.DANISH -> "Beskeden bliver oversat. Vent venligst...";
            case DiscordLocale.DUTCH -> "Het bericht wordt vertaald. Even geduld aub...";
            case DiscordLocale.FINNISH -> "Viestiä käännetään. Odota hetki...";
            case DiscordLocale.FRENCH -> "Le message est en cours de traduction. Veuillez patienter...";
            case DiscordLocale.GERMAN -> "Nachricht wird übersetzt. Bitte warten...";
            case DiscordLocale.GREEK -> "Το μήνυμα μεταφράζεται. Παρακαλώ περιμένετε...";
            case DiscordLocale.HINDI -> "संदेश का अनुवाद किया जा रहा है। कृपया प्रतीक्षा करें...";
            case DiscordLocale.HUNGARIAN -> "Az üzenet fordítása folyamatban van. Kérjük, várjon...";
            case DiscordLocale.INDONESIAN -> "Pesan sedang diterjemahkan. Mohon tunggu...";
            case DiscordLocale.ITALIAN -> "Il messaggio è in fase di traduzione. Si prega di attendere...";
            case DiscordLocale.JAPANESE -> "メッセージを翻訳しています。お待ちください...";
            case DiscordLocale.KOREAN -> "메시지가 번역 중입니다. 잠시만 기다려 주세요...";
            case DiscordLocale.LITHUANIAN -> "Žinutė verčiama. Prašome palaukti...";
            case DiscordLocale.NORWEGIAN -> "Meldingen blir oversatt. Vennligst vent...";
            case DiscordLocale.POLISH -> "Wiadomość jest tłumaczona. Proszę czekać...";
            case DiscordLocale.PORTUGUESE_BRAZILIAN -> "A mensagem está sendo traduzida. Por favor, aguarde...";
            case DiscordLocale.ROMANIAN_ROMANIA -> "Mesajul este în curs de traducere. Vă rugăm să așteptați...";
            case DiscordLocale.RUSSIAN -> "Сообщение переводится. Пожалуйста, подождите...";
            case DiscordLocale.SPANISH, DiscordLocale.SPANISH_LATAM -> "El mensaje se está traduciendo. Por favor espera...";
            case DiscordLocale.SWEDISH -> "Meddelandet översätts. Vänta gärna...";
            case DiscordLocale.THAI -> "กำลังแปลข้อความ กรุณารอสักครู่...";
            case DiscordLocale.TURKISH -> "Mesaj tercüme ediliyor. Lütfen bekleyin...";
            case DiscordLocale.UKRAINIAN -> "Повідомлення перекладається. Будь ласка, зачекайте...";
            case DiscordLocale.VIETNAMESE -> "Tin nhắn đang được dịch. Vui lòng đợi...";
            default -> "Message is being translated. Please wait...";
        };

        event.getMessage().reply(loadingMsg).queue(message -> {
            CompletableFuture.runAsync(() -> {
                try {
                    Payload payload = new Payload(null, TranslateType.DECORATED.getSystemPrompt(), Payload.userMessage(targetLocale, event.getMessage().getContentRaw()), 5000);
                    payload.translateAsync().thenAccept(result -> {
                        String reply = event.getMessage().getJumpUrl() + ": " + result;
                        message.editMessage(reply).queue();
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        EndUserError err = MessageInteraction.buildEndUserError((Exception) ex);
                        message.editMessage(err.getLocaleMessages().get(locale)).queue();
                        return null;
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    EndUserError err = MessageInteraction.buildEndUserError(ex);
                    message.editMessage(err.getLocaleMessages().get(locale)).queue();
                }
            });
        });
    }
}
