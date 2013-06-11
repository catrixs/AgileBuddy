package com.feiyang.agilebuddy.common;

import java.nio.ByteBuffer;

/**
 * Created by chenfei on 13-6-7.
 */
public class MessageDefinition {
    public static final byte INTENT_READY = 1;
    public static final byte INTENT_START = 2;
    public static final byte INTENT_OTHER_MOVE = 3;
    public static final byte INTENT_CHANGE_TURN = 4;

    public static final byte INTENT_GAME_OVER = 9;

    public static final byte TOOL_OBSTACLE = 10;

    public static final byte PLAYER_BLOOD_STATUS = 100;

    public static final String HOST = "192.168.1.100";
    public static final int SERVER_PORT = 8888;
    public static final int CLIENT_PORT = 9999;

    private static final byte TYPE_OFFSET = 0;
    private static final byte LENGTH_OFFSET = 1;
    public static final int VALUE_OFFSET = 3;

    private int offset;
    private byte[] buffer;
    private int length;

    public MessageDefinition(byte type) {
        offset = VALUE_OFFSET;
        buffer = new byte[128];
        buffer[0] = type;
    }

    public MessageDefinition(byte type, byte[] value) {
        buffer = new byte[VALUE_OFFSET + value.length];
        buffer[0] = type;
        System.arraycopy(value, 0, buffer, VALUE_OFFSET, value.length);
        offset = buffer.length;
        putMessageLength(buffer, (short) offset);
    }

    public MessageDefinition(byte[] message) {
        buffer = message;
        offset = VALUE_OFFSET;
        length = getMessageLength(buffer);
    }

    public void append(int value) {
        putInt(buffer, offset, value);
        offset += 4;
    }

    public int getInt() {
        int v = getInt(buffer, offset);
        offset += 4;
        return v;
    }

    public void append(float value) {
        putFloat(buffer, offset, value);
        offset += 4;
    }

    public float getFloat() {
        float v = getFloat(buffer, offset += 4);
        offset += 4;
        return v;
    }

    public boolean hasNext() {
        return offset <  length;
    }

    public byte getType() {
        return buffer[0];
    }

    public byte[] toMessage() {
        putMessageLength(buffer, (short) offset);
        return buffer;
    }

    public static short getMessageLength(byte[] message) {
        if (message == null || message.length < 3) {
            return 0;
        } else {
            return getShort(message, LENGTH_OFFSET);
        }
    }

    public static void putMessageLength(byte[] message, short length) {
        if (message == null) {
            return;
        }

        putShort(message, LENGTH_OFFSET, length);
    }


    /**
     * ����ת��Ϊ�ֽ�
     *
     * @param f
     * @return
     */
    public static void putFloat(byte[] b, int offset, float f) {
        checkBounder(b, offset, 4);
        int floatBits = Float.floatToIntBits(f);
        putInt(b, offset, floatBits);
    }

    /**
     * �ֽ�ת��Ϊ����
     *
     * @param b      �ֽڣ�����4���ֽڣ�
     * @param offset ��ʼλ��
     * @return
     */
    public static float getFloat(byte[] b, int offset) {
        checkBounder(b, offset, 4);
        int floatBits = getInt(b, offset);
        return Float.intBitsToFloat(floatBits);
    }

    /**
     * ��int���͵�����ת��Ϊbyte���� ԭ����int�����е��ĸ�byteȡ�����ֱ�洢
     *
     * @param num int����
     * @return ���ɵ�byte����
     */
    public static void putInt(byte[] b, int offset, int num) {
        checkBounder(b, offset, 4);

        b[offset + 0] = (byte) (num >>> 24);//ȡ���8λ�ŵ�0�±�
        b[offset + 1] = (byte) (num >>> 16);//ȡ�θ�8Ϊ�ŵ�1�±�
        b[offset + 2] = (byte) (num >>> 8); //ȡ�ε�8λ�ŵ�2�±�
        b[offset + 3] = (byte) (num);      //ȡ���8λ�ŵ�3�±�
    }

    /**
     * ��byte����ת��Ϊint����
     *
     * @param b �ֽ�����
     * @return ���ɵ�int����
     */
    public static int getInt(byte[] b, int offset) {
        checkBounder(b, offset, 4);

        int v0 = (b[offset + 0] & 0xff) << 24;//&0xff��byteֵ�޲���ת��int,����Java�Զ�����������,�ᱣ����λ�ķ���λ
        int v1 = (b[offset + 1] & 0xff) << 16;
        int v2 = (b[offset + 2] & 0xff) << 8;
        int v3 = (b[offset + 3] & 0xff);
        return v0 + v1 + v2 + v3;
    }

    /**
     * ת��shortΪbyte
     *
     * @param b
     * @param s     ��Ҫת����short
     * @param index
     */
    public static void putShort(byte b[], int index, short s) {
        checkBounder(b, index, 2);
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }

    /**
     * ͨ��byte����ȡ��short
     *
     * @param b
     * @param index �ڼ�λ��ʼȡ
     * @return
     */
    public static short getShort(byte[] b, int index) {
        checkBounder(b, index, 2);
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }

    private static void checkBounder(byte[] b, int offset, int length) {
        if ((offset + length) > b.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}
