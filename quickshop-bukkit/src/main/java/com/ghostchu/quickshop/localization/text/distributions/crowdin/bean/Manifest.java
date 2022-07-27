package com.ghostchu.quickshop.localization.text.distributions.crowdin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class Manifest {
    private List<String> files;
    private List<String> languages;
    private List<?> custom_languages;
    private long timestamp;
}
