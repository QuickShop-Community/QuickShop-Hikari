package com.ghostchu.quickshop.api.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;

public class CommandParser {
    private final String raw;
    private final List<String> args = new ArrayList<>();
    private final Map<String, List<String>> colonArgs = new LinkedHashMap<>();
    private StringBuilder buffer = new StringBuilder();

    public CommandParser(String raw) {
        this.raw = raw.trim();
        parse();
    }

    public List<String> getArgs() {
        return ImmutableList.copyOf(args);
    }

    public Map<String, List<String>> getColonArgs() {
        return ImmutableMap.copyOf(colonArgs);
    }

    public String getRaw() {
        return raw;
    }

    private void parse() {
        parseEscape();
        parseColon();
    }

    private void parseColon() {
        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String waiting = it.next();
            if (!waiting.contains(":")) continue;
            String[] spilt = waiting.split(":", 2);
            if (spilt.length < 2) continue;
            it.remove();
            List<String> registered = colonArgs.getOrDefault(spilt[0], new ArrayList<>());
            registered.add(spilt[1]);
            colonArgs.put(spilt[0].toLowerCase(Locale.ROOT), registered);
        }
    }

    private void parseEscape() {
        for (char c : raw.toCharArray()) {
            boolean commitBuffer = parseChar(c);
            if (commitBuffer) {
                if (!buffer.isEmpty()) {
                    args.add(buffer.toString());
                    buffer = new StringBuilder();
                }
            }
        }
        if (!buffer.isEmpty()) {
            args.add(buffer.toString());
        }
    }

    private boolean hangUp = false;

    private boolean parseChar(char c) {
        if (c == '`') {
            hangUp = !hangUp;
            return !hangUp;
        }
        if (c == ' ') {
            if (hangUp) {
                buffer.append(c);
            }
            return true;
        }
        buffer.append(c);
        return false;
    }
}
