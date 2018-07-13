package com.infomax.ibotncloudplayer.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by jy on 2016/11/22.
 * 发布版本--没有使用
 */
public class EncryptFileUtils {
    /**
     * 加密字节长度：Encrypt_Length
     */
    private static final int Encrypt_Length = 1024;
    /**
     * 异或常量 Constant_XOR ：0x08
     */
    private static final int Constant_XOR = 0x08;
    /**
     * 加解密 --执行一次解密，第二次执行就加密
     * 同步：
     * @param strFile 源文件绝对路径
     * @return
     */
    public synchronized static boolean decryption(String strFile) {
        int len = Encrypt_Length;
        try {
            File f = new File(strFile);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLen = raf.length();

            if (totalLen < Encrypt_Length)
            {
                len = (int) totalLen;
            }

            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, Encrypt_Length);
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = buffer.get(i);
                tmp = (byte) (rawByte ^ Constant_XOR);
                buffer.put(i, tmp);
            }
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
