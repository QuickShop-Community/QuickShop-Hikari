package org.maxgamer.quickshop.util.hack.packet;//package org.maxgamer.quickshop.util.hack.packet;
//
//import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
//import com.bergerkiller.bukkit.common.protocol.CommonPacket;
//import com.bergerkiller.bukkit.common.protocol.PacketType;
//import com.bergerkiller.bukkit.common.utils.BlockUtil;
//import com.bergerkiller.bukkit.common.utils.PacketUtil;
//import net.md_5.bungee.chat.ComponentSerializer;
//import org.apache.commons.lang3.Validate;
//import org.bukkit.Bukkit;
//import org.bukkit.block.Sign;
//import org.bukkit.entity.Player;
//import org.jetbrains.annotations.NotNull;
//import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.api.chat.ComponentPackage;
//
//import java.util.List;
//
///**
// * Little treat, we modify the sign component to translatable component if possible...
// */
//public class SignPacketHack {
//
//    private final QuickShop plugin;
//
//    public SignPacketHack(QuickShop plugin) {
//        this.plugin = plugin;
//        Validate.isTrue(Bukkit.getPluginManager().isPluginEnabled("BKCommonLib"), "Require BKCommonLib installed!");
//    }
//
//    public void sendLines(@NotNull Sign sign, @NotNull List<ComponentPackage> lines, Player player) {
//        if (player != null) {
//            CommonPacket updatePacket = BlockUtil.getUpdatePacket(sign);
//            if (updatePacket != null) {
//                applyToSign(updatePacket, lines);
//                PacketUtil.sendPacket(player, updatePacket);
//            }
//        }
//    }
//
//    public void applyToSign(@NotNull CommonPacket updatePacket, @NotNull List<ComponentPackage> lines) {
//        if (updatePacket.getType() == PacketType.OUT_TILE_ENTITY_DATA) {
//            // >= MC 1.10.2
//
//            CommonTagCompound compound = updatePacket.read(PacketType.OUT_TILE_ENTITY_DATA.data);
//            // ====================================================================
//
//            for (int i = 0; i < lines.size(); i++) {
//                String key = "Text" + (i + 1);
//                String text = ComponentSerializer.toString(lines.get(i).getComponents());
//                compound.putValue(key, text);
//            }
//
//            // ====================================================================
//            updatePacket.write(PacketType.OUT_TILE_ENTITY_DATA.data, compound);
//        }
//    }
//
//}
