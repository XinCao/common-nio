/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

import java.util.Arrays;

/**
 * 
 * @author 510655387@qq.com
 */
public class IPRange {

    public static void main(String... args) {
        IPRange ipRange = new IPRange("192.168.0.0", "192.168.0.255", "192.168.0.1");
        System.out.println(ipRange.isInRange("192.168.0.1"));
    }

    public static byte[] toByteArray(String address) {
        byte[] result = new byte[4];
        String[] strings = address.split("\\.");
        for (int i = 0, n = strings.length; i < n; i++) {
            result[i] = (byte) Integer.parseInt(strings[i]);
        }
        return result;
    }

    private static byte[] toBytes(long val) {
        byte[] result = new byte[4];
        result[3] = (byte) (val & 0xFF);
        result[2] = (byte) ((val >> 8) & 0xFF);
        result[1] = (byte) ((val >> 16) & 0xFF);
        result[0] = (byte) ((val >> 24) & 0xFF);
        return result;
    }

    private static long toLong(byte[] bytes) {
        long result = 0;
        result += (bytes[3] & 0xFF);
        result += ((bytes[2] & 0xFF) << 8);
        result += ((bytes[1] & 0xFF) << 16);
        result += (bytes[0] << 24);
        return result & 0xFFFFFFFFL;
    }

    private final byte[] address;

    private final long max;

    private final long min;

    public IPRange(byte[] min, byte[] max, byte[] address) {
        this.min = toLong(min);
        this.max = toLong(max);
        this.address = address;
    }

    /**
     * 点分十进制IP范围 new IPRange("192.168.0.0", "192.168.0.255", "192.168.0.1");
     *
     * @param min
     * @param max
     * @param address
     */
    public IPRange(String min, String max, String address) {
        this.min = toLong(toByteArray(min));
        this.max = toLong(toByteArray(max));
        this.address = toByteArray(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IPRange)) {
            return false;
        }
        IPRange ipRange = (IPRange) o;
        return max == ipRange.max && min == ipRange.min
                && Arrays.equals(address, ipRange.address);
    }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getMaxAsByteArray() {
        return toBytes(max);
    }

    public byte[] getMinAsByteArray() {
        return toBytes(min);
    }

    /**
     * Hashcode of IPRange object. Auto generated.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int result = (int) (min ^ (min >>> 32));
        result = 31 * result + (int) (max ^ (max >>> 32));
        result = 31 * result + Arrays.hashCode(address);
        return result;
    }

    public boolean isInRange(String address) {
        long addr = toLong(toByteArray(address));
        return addr >= min && addr <= max;
    }
}