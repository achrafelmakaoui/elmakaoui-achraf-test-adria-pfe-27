package com.adria.bank.chatbot.discord;

import com.adria.bank.chatbot.agent.BankAgent;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@ConditionalOnExpression("!'${discord.token:}'.isEmpty()")
public class DiscordBot extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);
    private static final int DISCORD_MAX_LENGTH = 2000;

    private final BankAgent agent;
    private final JDA jda;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public DiscordBot(BankAgent agent, @Value("${discord.token}") String token) throws InterruptedException {
        this.agent = agent;
        this.jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build()
                .awaitReady();
        log.info("Discord bot connected as {}", jda.getSelfUser().getName());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        Message message = event.getMessage();
        String text = message.getContentStripped().trim();
        if (text.isEmpty()) {
            return;
        }
        String conversationId = "discord-" + event.getAuthor().getId();
        event.getChannel().sendTyping().queue();
        executor.submit(() -> {
            String reply;
            try {
                reply = agent.chat(text, conversationId);
            } catch (Exception e) {
                log.error("Agent error for Discord user {}", event.getAuthor().getId(), e);
                reply = "Sorry, the assistant is temporarily unavailable. Please try again.";
            }
            if (reply == null || reply.isBlank()) {
                reply = "(no answer)";
            }
            if (reply.length() > DISCORD_MAX_LENGTH) {
                reply = reply.substring(0, DISCORD_MAX_LENGTH - 3) + "...";
            }
            message.reply(reply).queue();
        });
    }

    @PreDestroy
    public void shutdown() {
        jda.shutdown();
        executor.shutdown();
    }
}