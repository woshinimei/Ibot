package com.wifidirect;

import java.io.Serializable;

/**
 * Created by username on 2017/5/7.
 */

public class FileTransferBean implements Serializable {
    private static final long serialVersionUID = 0L;
    /** fileName */
    public String fileName;
    /** fileType  */
    public String fileType;
    /** file to byte array */
    public byte[] dataArray;
}
