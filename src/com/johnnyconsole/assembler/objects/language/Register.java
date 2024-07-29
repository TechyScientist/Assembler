package com.johnnyconsole.assembler.objects.language;

public class Register {

    private final String name;
    private final byte[] extended, base, high, low;

    public Register(String name) {
        this.name = name;
        extended = new byte[32];
        base = new byte[16];
        high = new byte[8];
        low = new byte[8];
    }

    public String name() {
        return name;
    }

    public int extended() {
        return toInt(extended);
    }

    public int base() {
        return toInt(base);
    }

    public int high() {
        return toInt(high);
    }

    public int low() {
        return toInt(low);
    }

    public void setExtended(int value) {
        setExtended(toBinaryArray(value, 32));
    }

    public void setBase(int value) {
        setBase(toBinaryArray(value, 16));
    }

    public void setHigh(int value) {
        setHigh(toBinaryArray(value, 8));
    }

    public void setLow(int value) {
        setLow(toBinaryArray(value, 8));
    }

    private void setExtended(byte[] bits) {
        System.arraycopy(bits, 0, extended, 0, extended.length);
        System.arraycopy(bits, 16, base, 0, base.length);
        System.arraycopy(bits, 16, high, 0, high.length);
        System.arraycopy(bits, 24, low, 0, low.length);
    }

    private void setBase(byte[] bits) {
        System.arraycopy(bits, 0, base, 0, base.length);
        System.arraycopy(bits, 0, extended, 16, bits.length);
        System.arraycopy(bits, 0, high, 0, high.length);
        System.arraycopy(bits, 16, low, 0, low.length);
    }

    private void setHigh(byte[] bits) {
        System.arraycopy(bits, 0, high, 0, high.length);
        System.arraycopy(bits, 0, base, 0, bits.length);
        System.arraycopy(bits, 0, extended, 16, bits.length);
    }

    private void setLow(byte[] bits) {
        System.arraycopy(bits, 0, low, 0, low.length);
        System.arraycopy(bits, 0, base, 8, bits.length);
        System.arraycopy(bits, 0, extended, 24, bits.length);
    }

    public void clear() {
        setExtended(0);
        setBase(0);
        setHigh(0);
        setLow(0);
    }

    private byte[] toBinaryArray(int value, int bits) {
        byte[] binary = new byte[bits];
        int bit = bits - 1;

        while(value > 0) {
            binary[bit--] = (byte)(value % 2);
            value /= 2;
        }
        return binary;
    }

    private int toInt(byte[] bits) {
        int value = 0;
        for (int i = 0; i < bits.length; i++) {
            if(bits[i] == 1) {
                value += (int)Math.pow(2, bits.length - i - 1);
            }
        }
        return value;
    }
}
