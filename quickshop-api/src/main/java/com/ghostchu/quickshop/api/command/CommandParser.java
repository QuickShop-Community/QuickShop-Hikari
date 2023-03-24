package com.ghostchu.quickshop.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * CommandParser is a utility class for parsing command arguments.
 */
public class CommandParser {
    private final String raw;
    private final List<String> args = new ArrayList<>();
    private final Map<String, List<String>> colonArgs = new LinkedHashMap<>();

    /**
     * Parse a command string.
     * @param raw raw command string - e.g. benefit add a b c d -tag:1 -tag:2 -foo:bar
     */
    public CommandParser(@NotNull String raw, boolean trimTail) {
        this.raw = raw;
        parse(trimTail);
    }

    /**
     * Gets the all arguments.
     * @return the all arguments. (benefit, a, b, c, d)
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * Gets the arguments started with `-`.
     * E.g [[tag,1], [tag,2], [foo,bar]]
     * @return the arguments started with `-`.
     */
    @NotNull
    public Map<String, List<String>> getColonArgs() {
        return colonArgs;
    }

    /**
     * The raw command string.
     * @return the raw command string.
     */
    @NotNull
    public String getRaw() {
        return raw;
    }

    private void parse(boolean trimTail) {
        explode(trimTail);
        parseColon();
    }

    private void parseColon() {
        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String waiting = it.next();
            if (!waiting.startsWith("-")) continue;
            if (!waiting.contains(":")) continue;
            String[] spilt = waiting.split(":", 2);
            if (spilt.length < 2) continue;
            if (spilt[0].isEmpty() || spilt[1].isEmpty()) continue;
            if (spilt[0].endsWith("\\")) continue;
            it.remove();
            List<String> registered = colonArgs.getOrDefault(spilt[0], new ArrayList<>());
            registered.add(spilt[1]);
            colonArgs.put(spilt[0].toLowerCase(Locale.ROOT), registered);
        }
    }

    private void explode(boolean trimTail) {
        StringBuilder buffer = new StringBuilder();
        for(char c: raw.toCharArray()){
            if(c == ' '){
                String newArg = buffer.toString();
                buffer = new StringBuilder();
                this.args.add(newArg);
            }else{
                buffer.append(c);
            }
        }
        if(buffer.isEmpty() && !trimTail){
            this.args.add(buffer.toString());
        }else if(!buffer.isEmpty()){
            this.args.add(buffer.toString());
        }
    }
}
