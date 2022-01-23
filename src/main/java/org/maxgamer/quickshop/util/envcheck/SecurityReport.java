package org.maxgamer.quickshop.util.envcheck;

import org.bukkit.plugin.PluginDescriptionFile;
import org.maxgamer.quickshop.util.Util;

import java.util.zip.ZipEntry;

public class SecurityReport {
    private final StringBuffer buffer = new StringBuffer();
    public void signatureFileMissing(String name){
        buffer.append("META-INF signature missing: ")
                .append("name=").append(name).append("\n");
    }
    public void signatureVerifyFail(ZipEntry zipEntry){
        buffer.append("Signature Fail: ")
                .append("name=").append(zipEntry.getName()).append(", ")
                .append("crc=").append(zipEntry.getCrc()).append(", ")
                .append("time=").append(zipEntry.getTime()).append(", ")
                .append("method=").append(zipEntry.getMethod()).append("\n");
    }
    public void potentialInfected(ZipEntry zipEntry){
        buffer.append("Potential Infected: ")
                .append("name=").append(zipEntry.getName()).append(", ")
                .append("crc=").append(zipEntry.getCrc()).append(", ")
                .append("time=").append(zipEntry.getTime()).append(", ")
                .append("method=").append(zipEntry.getMethod()).append("\n");
    }
    public void manifestModified(PluginDescriptionFile desc){
        buffer.append("Invalid Plugin Description: ")
                .append("name=").append(desc.getName()).append(", ")
                .append("main=").append(desc.getMain()).append(", ")
                .append("libraries=[").append(Util.list2String(desc.getLibraries())).append("], ")
                .append("provides=[").append(Util.list2String(desc.getProvides())).append("]\n");
    }
    public String bake(){
        return "=============================\n" +
                "  QuickShop Security Report\n" +
                "=============================\n" +
                "Description:\n" +
                "    QuickShop detected self jar has been modified.\n" +
                "    Learn more: https://github.com/PotatoCraft-Studio/QuickShop-Reremake/wiki/QuickShop-halt-my-server\n" +
                "\n" +
                "What that mean: \n" +
                "    It usually mean the jar has been infected by malware and cannot be trusted.\n" +
                "    Make sure you downloading jar from our SpigotMC and CodeMC pages.\n" +
                "    Any other sources is untrusted and may provide a infected jar.\n" +
                "\n" +
                "I download QuickShop from SpigotMC, why I still see this:\n" +
                "    As far as we know, some malware like \"L10\" have ability to infect other jars that installed on your server.\n" +
                "    So clean jar can be infect when you installed on infected server.\n" +
                "\n" +
                "What should I do:\n" +
                "    Backup your server immediately, execute virus scan with regular anti-malware software and Spigot Anti-Malware tool.\n" +
                "    You can download SAMT here: https://www.spigotmc.org/resources/spigot-anti-malware.64982/\n" +
                "\n" +
                "Could this be a false positive:\n" +
                "    Basically not.\n" +
                "\n" +
                "I need some help to handle this issue:\n" +
                "    Our community and developer can help you and give you action suggestion.\n" +
                "    Join our discord support server: https://discord.gg/bfefw2E\n" +
                "\n" +
                "Can I disable this noisy check:\n" +
                "    No, you can't.\n" +
                "\n" +
                "----------------------------------\n" +
                "Important information related to this issue.\n" +
                "When you ask for support, please provide this paste to the support staffs:\n" +
                "\n"+ buffer;
    }
}
