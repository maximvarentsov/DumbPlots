package com.turt2live.dumbplots.util;

import com.turt2live.dumbplots.plot.LinearSide;
import com.turt2live.dumbplots.plot.corner.CornerType;

public class Conditional {

    public static enum ConditionalType {
        CORNER, SIDE;
    }

    public static final Conditional NO_CONDITION = null;

    private CornerType corner;
    private LinearSide side;
    private ConditionalType type;

    public Conditional(CornerType corner) {
        this.corner = corner;
        type = ConditionalType.CORNER;
    }

    public Conditional(LinearSide side) {
        this.side = side;
        type = ConditionalType.SIDE;
    }

    public ConditionalType getType() {
        return type;
    }

    public boolean match(CornerType corner) {
        return this.corner == corner;
    }

    public boolean match(LinearSide side) {
        return this.side == side;
    }

}
