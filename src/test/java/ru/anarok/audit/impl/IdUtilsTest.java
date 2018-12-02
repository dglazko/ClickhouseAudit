package ru.anarok.audit.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.anarok.audit.impl.IdUtils;

class IdUtilsTest {

    @Test
    public void testIds() {
        IdUtils utils = new IdUtils();

        String fullTimeStamp = printBits(utils.combineIds((long) Math.pow(2, 40) - 1, (byte) 0, (short) 0));
        Assertions.assertEquals("1111111111111111111111111111111111111111000000000000000000000000", fullTimeStamp, "Timestamp field overflow");

        String fullClientId = printBits(utils.combineIds((long) 0, (byte) ((byte) Math.pow(2, 8) - 1), (short) 0));
        Assertions.assertEquals("111111110000000000000000", fullClientId, "Client ID field overflow");

        String fullRowId = printBits(utils.combineIds((long) 0, (byte) 0, (short) ((short) Math.pow(2, 16) - 1)));
        Assertions.assertEquals("1111111111111111", fullRowId, "Local row ID field overflow");
    }

    private String printBits(long value) {
        return (Long.toBinaryString(value));
    }

}