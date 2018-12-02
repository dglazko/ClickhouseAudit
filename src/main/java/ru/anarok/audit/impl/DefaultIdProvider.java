package ru.anarok.audit.impl;

import ru.anarok.audit.api.ClickhouseIdProvider;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultIdProvider implements ClickhouseIdProvider {
    private final static int MIN_LOCAL_ID = 0;
    private final static int MAX_LOCAL_ID = (int) Math.pow(2, 16) - 1;
    private final static int CLIENT_MASK = 0b11111111;
    private final static int LOCAL_ID_MASK = 0b1111111111111111;
    private final byte clientId;
    private AtomicInteger localId = new AtomicInteger(MIN_LOCAL_ID);

    public DefaultIdProvider() {
        String[] vmName = ManagementFactory.getRuntimeMXBean().getName().split("@");
        if (vmName.length >= 1 && vmName[0].matches("/$[0-9]+^/")) {
            this.clientId = (byte) (Integer.valueOf(vmName[0]) & CLIENT_MASK);
        } else this.clientId = (byte) (new Random()).nextInt((int) Math.pow(2, 8));
    }

    /**
     * Generate new global record id.
     * <p>
     * Compound ids are used for this function.
     * <p>
     * |-------------|-------------|--------------|
     * |   40 bit    |    8  bit   |    16 bit    |
     * |------------------------------------------|
     * |  timestamp  | client ID   | local row ID |
     * |------------------------------------------|
     *
     * @return newly generated global row id
     */
    public long newRecordId() {
        long timestamp = System.currentTimeMillis() / 1000;
        short localRecordId = getLocalRowId();

        return combineIds(timestamp, clientId, localRecordId);
    }

    private short getLocalRowId() {
        return (short) localId.getAndUpdate(operand -> {
            if (operand == MAX_LOCAL_ID)
                return MIN_LOCAL_ID;
            else
                return operand + 1;
        });
    }

    long combineIds(long timestamp, byte clientId, short localRecordId) {
        return (timestamp << 24) + ((clientId & CLIENT_MASK) << 16) + (localRecordId & LOCAL_ID_MASK);
    }
}
