package com.ghostchu.quickshop.localization.text.distributions.crowdin.bean;

import lombok.Data;

import java.util.List;


@Data
public class Manifest {
    private List<String> files;
    private List<String> languages;
    private List<?> custom_languages;
    private long timestamp;

    public Manifest(List<String> files, List<String> languages, List<?> custom_languages, long timestamp) {
        this.files = files;
        this.languages = languages;
        this.custom_languages = custom_languages;
        this.timestamp = timestamp;
    }

    public Manifest() {
    }
}
