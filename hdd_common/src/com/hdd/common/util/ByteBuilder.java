package com.hdd.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBuilder implements java.io.Serializable {

    /** use serialVersionUID for interoperability */
    private static final long serialVersionUID = 4320685877147921099L;

    private static Logger log = LoggerFactory.getLogger(ByteBuilder.class);
    protected static final char[] hex = "0123456789ABCDEF".toCharArray();
    /**
     * The value is used for byte storage.
     */
    protected byte value[];

    /**
     * The count is the number of byte used.
     */
    protected int count;
    /**
     * The postion is the number of byte get.
     */
    protected int postion;

    /**
     * Constructs a byte builder with no byte in it and an initial capacity
     * specified by the <code>capacity</code> argument.
     * 
     * @param capacity
     *            the initial capacity.
     * @throws NegativeArraySizeException
     *             if the <code>capacity</code> argument is less than
     *             <code>0</code>.
     */
    public ByteBuilder(int capacity) {
        if (capacity <= 0) {
            throw new NegativeArraySizeException("capacity can not less than zero!");
        }
        value = new byte[capacity];
        postion = 0;
        count = 0;
    }

    public ByteBuilder() {
        value = new byte[16];
        postion = 0;
        count = 0;
    }

    /**
     * Returns the length (byte count).
     * 
     * @return the length of the sequence of bytes currently represented by this
     *         object
     */
    public int length() {
        return count;
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage
     * available for newly inserted bytes, beyond which an allocation will
     * occur.
     * 
     * @return the current capacity
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum. If
     * the current capacity is less than the argument, then a new internal array
     * is allocated with greater capacity. The new capacity is the larger of:
     * <ul>
     * <li>The <code>minimumCapacity</code> argument.
     * <li>Twice the old capacity, plus <code>2</code>.
     * </ul>
     * If the <code>minimumCapacity</code> argument is nonpositive, this method
     * takes no action and simply returns.
     * 
     * @param minimumCapacity
     *            the minimum desired capacity.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * This implements the expansion semantics of ensureCapacity with no size
     * check or synchronization.
     */
    void expandCapacity(int minimumCapacity) {
        int newCapacity = (value.length + 1) * 2;
        if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
        } else if (minimumCapacity > newCapacity) {
            newCapacity = minimumCapacity;
        }
        value = Arrays.copyOf(value, newCapacity);
    }

    /**
     * Attempts to reduce storage used for the byte sequence. If the buffer is
     * larger than necessary to hold its current sequence of bytes, then it may
     * be resized to become more space efficient. Calling this method may, but
     * is not required to, affect the value returned by a subsequent call to the
     * {@link #capacity()} method.
     */
    public void trimToSize() {
        if (count < value.length) {
            value = Arrays.copyOf(value, count);
        }
    }

    /**
     * Sets the length of the byte sequence. The sequence is changed to a new
     * byte sequence whose length is specified by the argument. For every
     * nonnegative index <i>k</i> less than <code>newLength</code>, the byte at
     * index <i>k</i> in the new byte sequence is the same as the byte at index
     * <i>k</i> in the old sequence if <i>k</i> is less than the length of the
     * old byte sequence; otherwise, it is the null byte
     * <code>'&#92;u0000'</code>.
     * 
     * In other words, if the <code>newLength</code> argument is less than the
     * current length, the length is changed to the specified length.
     * <p>
     * If the <code>newLength</code> argument is greater than or equal to the
     * current length, sufficient null bytes (<code>'&#92;u0000'</code>) are
     * appended so that length becomes the <code>newLength</code> argument.
     * <p>
     * The <code>newLength</code> argument must be greater than or equal to
     * <code>0</code>.
     * 
     * @param newLength
     *            the new length
     * @throws IndexOutOfBoundsException
     *             if the <code>newLength</code> argument is negative.
     */
    public void setLength(int newLength) {
        if (newLength < 0)
            throw new StringIndexOutOfBoundsException(newLength);
        if (newLength > value.length)
            expandCapacity(newLength);

        if (count < newLength) {
            for (; count < newLength; count++)
                value[count] = '\0';
        } else {
            count = newLength;
        }
    }

    /**
     * Bytes are copied from this sequence into the destination byte array
     * <code>dst</code>. The first byte to be copied is at index
     * <code>srcBegin</code>; the last byte to be copied is at index
     * <code>srcEnd-1</code>. The total number of bytes to be copied is
     * <code>srcEnd-srcBegin</code>. The bytes are copied into the subarray of
     * <code>dst</code> starting at index <code>dstBegin</code> and ending at
     * index:
     * <p>
     * <blockquote>
     * 
     * <pre>
     * dstbegin + (srcEnd - srcBegin) - 1
     * </pre>
     * 
     * </blockquote>
     * 
     * @param srcBegin
     *            start copying at this offset.
     * @param srcEnd
     *            stop copying at this offset.
     * @param dst
     *            the array to copy the data into.
     * @param dstBegin
     *            offset into <code>dst</code>.
     * @throws NullPointerException
     *             if <code>dst</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if any of the following is true:
     *             <ul>
     *             <li><code>srcBegin</code> is negative <li><code>dstBegin
     *             </code> is negative <li>the <code>srcBegin</code> argument is
     *             greater than the <code>srcEnd</code> argument. <li><code>
     *             srcEnd</code> is greater than <code>this.length()</code>. 
     *             <li><code>dstBegin+srcEnd-srcBegin</code> is greater than
     *             <code>dst.length</code>
     *             </ul>
     */
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        if (srcBegin < 0)
            throw new IndexOutOfBoundsException("" + srcBegin);
        if ((srcEnd < 0) || (srcEnd > count))
            throw new IndexOutOfBoundsException("" + srcEnd);
        if (srcBegin > srcEnd)
            throw new IndexOutOfBoundsException("srcBegin > srcEnd");
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    public ByteBuilder append(byte byteValue) {
        byte[] byteArray = new byte[1];
        byteArray[0] = byteValue;
        return this.append(byteArray);
    }

    public ByteBuilder append(byte[] byteArray) {
        if (null == byteArray || byteArray.length <= 0)
            return this;
        int len = byteArray.length;
        int newcount = count + len;
        if (newcount > value.length)
            expandCapacity(newcount);
        System.arraycopy(byteArray, 0, value, count, len);
        count = newcount;
        return this;
    }

    public ByteBuilder append(ByteBuilder bb) {
        if (bb == null)
            return this;
        int len = bb.length();
        int newcount = count + len;
        if (newcount > value.length)
            expandCapacity(newcount);
        bb.getBytes(0, len, value, count);
        count = newcount;
        return this;
    }

    public ByteBuilder append(ByteBuffer bb) {
        if (bb == null || bb.capacity() <= 0)
            return this;
        byte[] byteArray = bb.array();
        return this.append(byteArray);
    }

    public ByteBuilder append(char c) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) (c >> 8);
        byteArray[1] = (byte) (c >> 0);
        return this.append(byteArray);
    }

    public ByteBuilder append(short s) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) (s >> 8);
        byteArray[1] = (byte) (s >> 0);
        return this.append(byteArray);
    }

    public ByteBuilder append(int i) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (i >> 24);
        byteArray[1] = (byte) (i >> 16);
        byteArray[2] = (byte) (i >> 8);
        byteArray[3] = (byte) (i >> 0);
        return this.append(byteArray);
    }

    public ByteBuilder append(long l) {
        byte[] byteArray = new byte[8];
        byteArray[0] = (byte) (l >> 56);
        byteArray[1] = (byte) (l >> 48);
        byteArray[2] = (byte) (l >> 40);
        byteArray[3] = (byte) (l >> 32);
        byteArray[4] = (byte) (l >> 24);
        byteArray[5] = (byte) (l >> 16);
        byteArray[6] = (byte) (l >> 8);
        byteArray[7] = (byte) (l >> 0);
        return this.append(byteArray);
    }

    public ByteBuilder append(float f) {
        int i = Float.floatToRawIntBits(f);
        return this.append(i);
    }

    public ByteBuilder append(double d) {
        long l = Double.doubleToRawLongBits(d);
        return this.append(l);
    }

    public ByteBuilder append(String s) {
        byte[] byteValue = s.getBytes();
        return this.append(byteValue);
    }

    public ByteBuilder append(String s, String codingPatten) {
        byte[] byteValue;
        try {
            byteValue = s.getBytes(codingPatten);
        } catch (UnsupportedEncodingException e) {
            log.error("String coding patten error!", e);
            return this;
        }
        return this.append(byteValue);
    }

    /**
     * 
     * 添加由参数<code>length</code>指定长度的字符串，若<code>length</code>
     * 小于字符串的实际长度，则以字符串的长度为准， 若<code>length</code>大于字符串的实际长度，则字符串之外以空字符填充
     * 
     * @author David
     * @since: 2012-4-9
     */
    public ByteBuilder append(String s, int length, String codingPattern) {
        if (null == s || "".equals(s) || length <= 0) {
            return this;
        }
        byte[] arr;
        try {
            arr = s.getBytes(codingPattern);
        } catch (UnsupportedEncodingException e) {
            log.error("Coding pattern[" + codingPattern + "] is unsupported!", e);
            return this;
        }
        byte[] dest = new byte[length];
        int len = arr.length;
        int copyLen = len > length ? length : len;
        System.arraycopy(arr, 0, dest, 0, copyLen);
        return this.append(dest);
    }

    public ByteBuilder appendHex(String hexStr) throws Exception {
        if (null == hexStr || "".equals(hexStr)) {
            return this;
        } else {
            if (hexStr.length() % 2 != 0) {
                throw new Exception("hexStr's length not double 2!");
            }
            for (int i = 0; i < hexStr.length(); i += 2) {
                byte temp = Byte.parseByte(hexStr.substring(i, i + 2), 16);
                this.append(temp);
            }
        }
        return this;
    }

    public ByteBuilder appendHex(String hexStr, int length) throws Exception {
        if (length <= 0) {
            return this;
        }
        if (null == hexStr || "".equals(hexStr)) {
            for (int i = 0; i < length; i++) {
                this.append(new Byte("0"));
            }
        } else {
            if (hexStr.length() % 2 != 0) {
                throw new Exception("hexStr's length not double 2!");
            }
            byte[] byteArray = new byte[length];
            int index = 0;
            for (int i = 0; i < hexStr.length(); i += 2) {
                byte temp = (byte) Short.parseShort(hexStr.substring(i, i + 2), 16);
                byteArray[index++] = temp;
            }
            for (int i = 0; i < length - hexStr.length() / 2; i++) {
                byteArray[index++] = 0x0;
            }
            this.append(byteArray);
        }
        return this;
    }

    public byte getByte() {
        if (postion > count) {
            throw new IndexOutOfBoundsException("index[" + postion + "] out of bounds[" + count + "]");
        }
        return this.value[postion++];
    }

    public byte[] getBytes(int length) {
        if (length <= 0) {
            throw new NegativeArraySizeException("length can not less than zero!");
        }
        if (postion + length > count) {
            int index = postion + length;
            throw new IndexOutOfBoundsException("index[" + index + "] out of bounds[" + count + "]");
        }
        byte[] dest = new byte[length];
        System.arraycopy(value, postion, dest, 0, length);
        postion += length;
        return dest;
    }

    public short getShort() {
        byte[] bytes = this.getBytes(2);
        short value = (short) ((bytes[0] << 8) | (bytes[1] & 0xff));
        return value;
    }

    public int getInt() {
        byte[] bytes = this.getBytes(4);
        int value = ((bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | (bytes[3] & 0xff));
        return value;
    }

    public long getLong() {
        byte[] bytes = this.getBytes(8);
        long value = ((bytes[0] << 56) | (bytes[1] << 48) | (bytes[2] << 40) | (bytes[3] << 32) | (bytes[4] << 24)
                | (bytes[5] << 16) | (bytes[6] << 8) | (bytes[7] & 0xff));
        return value;
    }

    public String getStr(int length) {
        if (length <= 0) {
            throw new NegativeArraySizeException("length can not less than zero!");
        }
        byte[] bytes = this.getBytes(length);
        int i = -1;
        while (bytes[++i] != 0 && i < length - 1) {
            ;
        }
        return new String(bytes, 0, i);
    }

    public String getStr(int length, String codingPattern) {
        if (length <= 0) {
            throw new NegativeArraySizeException("length can not less than zero!");
        }
        byte[] bytes = this.getBytes(length);
        int i = -1;
        while (bytes[++i] != 0 && i < length - 1) {
            ;
        }
        try {
            return new String(bytes, 0, i, codingPattern);
        } catch (UnsupportedEncodingException e) {
            log.error("Coding pattern[" + codingPattern + "] is unsupported!", e);
            return null;
        }
    }

    public String getHex(int length) {
        if (length <= 0) {
            throw new NegativeArraySizeException("length can not less than zero!");
        }
        byte[] bytes = this.getBytes(length);
        StringBuffer str = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            str.append(hex[(bytes[i] >> 4) & 0x0f]);
            str.append(hex[bytes[i] & 0x0f]);
        }
        return str.toString();
    }

    public void reset() {
        this.count = 0;
        this.postion = 0;
        // this.value = null;
    }

    public byte[] toArray() {
        this.trimToSize();
        return this.value;
    }

    /**
     * 读取一行数据(以separator分割)
     * 
     * @author David
     * @since: 2012-4-19
     */
    public byte[] getLine(byte[] separator) {
        if (null == separator || separator.length == 0) {
            log.warn("separator is empty!");
            return null;
        }
        int length = separator.length;
        ByteBuilder bb = new ByteBuilder();
        byte[] temp = getBytes(length);
        while (null != temp && temp.length != 0) {
            if (!equal(temp, separator)) {
                bb.append(temp);
                temp = getBytes(length);
            } else {
                break;
            }
        }
        return bb.toArray();
    }

    private boolean equal(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        } else {
            int index = left.length - 1;
            while (index > 0) {
                if (left[index] != right[index]) {
                    return false;
                }
                index--;
            }
            return true;
        }
    }
}
