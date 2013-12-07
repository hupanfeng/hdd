package com.hdd.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author David
 * @version 1.0
 * 
 */
public class CycleByteBuilder extends ByteBuilder {
    private static Logger log = LoggerFactory.getLogger(CycleByteBuilder.class);
    //
    private static final long serialVersionUID = 1L;

    /**
     * In cycle buffer mode,the boundary indicates the max "count"
     */
    private int boundary;
    /**
     * 用于标记读写谁领先
     */
    private int flag;

    public CycleByteBuilder() {
        super();
        boundary = 16;
        this.flag = 0;
    }

    public CycleByteBuilder(int boundary) {
        super(boundary);
        this.boundary = boundary;
        this.flag = 0;
    }

    /**
     * This implements the expansion semantics of ensureCapacity with no size
     * check or synchronization.
     */
    void expandCapacity() {
        // 备份原始数据
        int oldLength = getValidateCapacity();
        byte[] oldBytes = getBytes(oldLength);

        int newBoundary = boundary * 2;
        boundary = newBoundary;
        count = 0;
        postion = 0;
        value = new byte[boundary];
        if (null != oldBytes && oldBytes.length > 0) {
            append(oldBytes);
        }
    }

    /**
     * 
     * 在环状模式下，填充缓存，若至边界则从头循环填充
     * 
     * @author David
     * @since: 2012-4-16
     */
    @Override
    public ByteBuilder append(byte[] byteArray) {
        if (null == byteArray || byteArray.length <= 0)
            return this;
        int length = byteArray.length;
        // 判断是否需要扩容
        if (byteArray.length > getLeftCapacity()) {
            expandCapacity();
        }
        // 判断是否可能要跨边界
        if (count + length > boundary) {
            flag++;
            int endLength = boundary - count;
            System.arraycopy(byteArray, 0, value, count, endLength);
            int beginLength = length - endLength;
            System.arraycopy(byteArray, endLength, value, 0, beginLength);
            count = (0 + beginLength) % boundary;
        } else {
            System.arraycopy(byteArray, 0, value, count, length);
            count = (count + length) % boundary;
            if (count == 0) {
                flag++;
            }
        }
        return this;
    }

    /**
     * 循环从环状buffer里读取数据
     * 
     * @author David
     * @since: 2012-4-14
     */
    @Override
    public byte[] getBytes(int length) {
        if (length <= 0) {
            log.warn("You really want to read nothing?!");
            return null;
        }
        int validateLength = this.getValidateCapacity();
        length = length > validateLength ? validateLength : length;
        byte[] ret = new byte[length];
        // 判断是否要跨边界读取
        if (postion + length > boundary) {
            flag--;
            int endLength = boundary - postion;
            System.arraycopy(value, postion, ret, 0, endLength);
            int beginLength = length - endLength;
            System.arraycopy(value, 0, ret, endLength, beginLength);
            postion = (0 + beginLength) % boundary;
        } else {
            System.arraycopy(value, postion, ret, 0, length);
            postion = (postion + length) % boundary;
            if (postion == 0) {
                flag--;
            }
        }
        return ret;
    }

    /**
     * 
     * 获取剩余容量
     * 
     * @author David
     * @since: 2012-4-17
     */
    private int getLeftCapacity() {
        return postion > count ? postion - count : boundary + postion - count;
    }

    /**
     * 
     * 获取有效容量（即有多少数据可读取）
     * 
     * @author David
     * @since: 2012-4-17
     */
    private int getValidateCapacity() {
        return count > postion ? count - postion : (flag == 0 ? count - postion : boundary + count - postion);
    }
}
