package org.maxgamer.quickshop.shop;

public class InteractionController {


    enum Interaction{
        STANDING_LEFT_CLICK_SIGN,
        STANDING_RIGHT_CLICK_SIGN,
        STANDING_LEFT_CLICK_SHOPBLOCK,
        STANDING_RIGHT_CLICK_SHOPBLOCK,
        SNEAKING_LEFT_CLICK_SIGN,
        SNEAKING_RIGHT_CLICK_SIGN,
        SNEAKING_LEFT_CLICK_SHOPBLOCK,
        SNEAKING_RIGHT_CLICK_SHOPBLOCK
    }
    /**
     * The shop that this controller is controlling
     * In normal interaction trade, BUY and SELL have same behavior which ask the user what they want to do.
     * In direct trade mode, BUY will use execute buying directly and SELL will use execute selling directly.
     * In direct trade case, the user will not be asked anything.
     *
     * CONTROL_PANEL will show up a Shop Control Panel which will allow the user to change the shop's settings.
     */
    enum InteractionBehavior {
        BUY,
        SELL,
        CONTROL_PANEL,
        NONE
    }
}
