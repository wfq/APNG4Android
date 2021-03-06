package com.yupaopao.animation.apng.chunk;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: Length (长度)	4字节	指定数据块中数据域的长度，其长度不超过(231－1)字节
 * Chunk Type Code (数据块类型码)	4字节	数据块类型码由ASCII字母(A-Z和a-z)组成
 * Chunk Data (数据块数据)	可变长度	存储按照Chunk Type Code指定的数据
 * CRC (循环冗余检测)	4字节	存储用来检测是否有错误的循环冗余码
 * @Link https://www.w3.org/TR/PNG
 * @Author: pengfei.zhou
 * @CreateDate: 2019/3/27
 */
class Chunk {
    int length;
    int type;
    byte[] data;
    int crc;
    private static ThreadLocal<byte[]> __intBytes = new ThreadLocal<>();

    void parse() {
    }

    static Chunk read(InputStream inputStream, boolean skipData) throws IOException {
        if (Thread.interrupted()) {
            return null;
        }
        int length = readIntFromInputStream(inputStream);
        int type = readIntFromInputStream(inputStream);
        Chunk chunk = newInstance(type);
        chunk.type = type;
        chunk.length = length;
        if (skipData && (chunk instanceof IDATChunk || chunk instanceof FDATChunk)) {
            inputStream.skip(length + 4);
        } else {
            chunk.data = new byte[chunk.length];
            inputStream.read(chunk.data);
            chunk.crc = readIntFromInputStream(inputStream);
            chunk.parse();
        }
        return chunk;
    }

    private static Chunk newInstance(int typeCode) {
        Chunk chunk;
        switch (typeCode) {
            case IHDRChunk.ID:
                chunk = new IHDRChunk();
                break;
            case IDATChunk.ID:
                chunk = new IDATChunk();
                break;
            case IENDChunk.ID:
                chunk = new IENDChunk();
                break;
            case ACTLChunk.ID:
                chunk = new ACTLChunk();
                break;
            case FCTLChunk.ID:
                chunk = new FCTLChunk();
                break;
            case FDATChunk.ID:
                chunk = new FDATChunk();
                break;
            default:
                chunk = new Chunk();
                break;
        }
        return chunk;
    }

    private static byte[] ensureBytes() {
        byte[] bytes = __intBytes.get();
        if (bytes == null) {
            bytes = new byte[4];
            __intBytes.set(bytes);
        }
        return bytes;
    }

    static int readIntFromInputStream(InputStream inputStream) throws IOException {
        inputStream.read(ensureBytes());
        return byteArrayToInt(ensureBytes());
    }

    static String readTypeCodeFromInputStream(InputStream inputStream) throws IOException {
        inputStream.read(ensureBytes());
        return new String(ensureBytes());
    }


    private static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    int getRawDataLength() {
        return length + 12;
    }

    int peekData(int i) {
        return data[i];
    }

    void copy(byte[] dst, int offset) {
        copyLength(dst, offset);
        offset += 4;
        copyTypeCode(dst, offset);
        offset += 4;
        if (data != null && data.length > 0) {
            copyData(dst, offset);
            offset += length;
        }
        copyCrc(dst, offset);
    }

    void copyLength(byte[] dst, int offset) {
        dst[offset] = readIntByByte(length, 0);
        dst[offset + 1] = readIntByByte(length, 1);
        dst[offset + 2] = readIntByByte(length, 2);
        dst[offset + 3] = readIntByByte(length, 3);
    }

    void copyTypeCode(byte[] dst, int offset) {
        dst[offset] = readIntByByte(type, 0);
        dst[offset + 1] = readIntByByte(type, 1);
        dst[offset + 2] = readIntByByte(type, 2);
        dst[offset + 3] = readIntByByte(type, 3);
    }

    void copyData(byte[] dst, int offset) {
        System.arraycopy(data, 0, dst, offset, length);
    }

    void copyCrc(byte[] dst, int offset) {
        dst[offset] = readIntByByte(crc, 0);
        dst[offset + 1] = readIntByByte(crc, 1);
        dst[offset + 2] = readIntByByte(crc, 2);
        dst[offset + 3] = readIntByByte(crc, 3);
    }


    static byte readIntByByte(int val, int index) {
        if (index == 0) {
            return (byte) ((val >> 24) & 0xff);
        } else if (index == 1) {
            return (byte) ((val >> 16) & 0xff);
        } else if (index == 2) {
            return (byte) ((val >> 8) & 0xff);
        } else {
            return (byte) (val & 0xff);
        }
    }
}
