package com.ghostchu.quickshop.localization.text.distributions.crowdin;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OTAException extends Exception {
    public OTAException(String message) {
        super(message);
    }
}
