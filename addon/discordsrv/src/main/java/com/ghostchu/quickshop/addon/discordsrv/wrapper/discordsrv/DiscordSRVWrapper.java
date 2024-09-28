package com.ghostchu.quickshop.addon.discordsrv.wrapper.discordsrv;

import com.ghostchu.quickshop.addon.discordsrv.wrapper.JDAWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Wrapper to convert between net.dv8tion.jda.api stuff to
 * github.scarsz.discordsrv.dependencies.jda.api stuff
 */
public class DiscordSRVWrapper implements JDAWrapper {

  public DiscordSRVWrapper() {
    //DiscordSRV.api.subscribe(this);
  }

  @Override
  public boolean isBind(@NotNull final UUID player) {

    final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player);
    return discordId != null;
  }

  @Override
  public void sendMessage(@NotNull final UUID player, @NotNull final String message) {

    final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player);
    if(discordId == null) {
      return;
    }
    DiscordUtil.getUserById(discordId).openPrivateChannel()
            .queue((channel)->channel.sendMessage(message).queue());
  }


  @Override
  public void sendMessage(@NotNull final UUID player, @NotNull final MessageEmbed message) {

    final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player);
    if(discordId == null) {
      return;
    }
    DiscordUtil.getUserById(discordId).openPrivateChannel().queue((channel)->channel.sendMessageEmbeds(message).queue());
  }

  @Override
  public void sendChannelMessage(@NotNull final String channelId, @NotNull final String message) {

    final GuildChannel channel = DiscordSRV.getPlugin().getJda().getGuildChannelById(channelId);
    if(channel == null) {
      return;
    }
    if(!(channel instanceof TextChannel textChannel)) {
      return;
    }
    textChannel.sendMessage(message).queue();

  }

  @Override
  public void sendChannelMessage(@NotNull final String channelId, @NotNull final MessageEmbed message) {

    final GuildChannel channel = DiscordSRV.getPlugin().getJda().getGuildChannelById(channelId);
    if(channel == null) {
      return;
    }
    if(!(channel instanceof TextChannel textChannel)) {
      return;
    }
    textChannel.sendMessageEmbeds(message).queue();
  }
}
