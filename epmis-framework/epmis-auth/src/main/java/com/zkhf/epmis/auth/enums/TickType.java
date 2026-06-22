package com.zkhf.epmis.auth.enums;

public enum TickType {
    USER(0),
    NICE(1),
    SYSTEM(2),
    IDLE(3),
    IOWAIT(4),
    IRQ(5),
    SOFTIRQ(6),
    STEAL(7);

    private final int index;

    private TickType(int value) {
        this.index = value;
    }

    public int getIndex() {
        return this.index;
    }
}
