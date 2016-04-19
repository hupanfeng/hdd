package com.hdd.common.util;

import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringTokenizer;

public class FTPClientDecorator extends FTPClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 控制字符集编码
     */
    static final String CONTROL_ENCODING = "GB2312";
    /**
     * ftp的文件分隔符
     */
    static final String FTP_SEPARATOR = "/";
    /**
     * 本地文件的分隔符
     */
    static final String LOCAL_FILE_SEPARATOR = File.separator;
    /**
     * 文件名和文件后缀间的分隔符
     */
    static final String DOT_SUFFIX = ".";
    /**
     * ACSII传输方式
     */
    static final String TRANSFER_ASCII = "A";
    /**
     * 二进制传入方式
     */
    static final String TRANSFER_IMAGE = "I";

    /**
     * 构造方法，设置控制字符集编码为GB2312
     */
    public FTPClientDecorator(boolean zhDate) {
        super();
        super.setControlEncoding(CONTROL_ENCODING);
        if (zhDate) {
            super.configure(new FTPClientConfig("com.zte.protocol.MyFTPEntryParser"));
        }
    }

    /**
     * 构造方法
     */
    public FTPClientDecorator() {
        new FTPClientDecorator(false);
    }

    /**
     * 与ftp服务器建立连接 (默认端口和无限超时时间)
     *
     * @param host     ftp服务器主机
     * @param user     用户名
     * @param password 密码
     * @return
     */
    public boolean connect(String host, String user, String password) throws IOException {
        return connect(host, -1, user, password, -1);
    }

    /**
     * 与ftp服务器建立连接
     *
     * @param host     ftp服务器主机
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @return
     */
    public boolean connect(String host, int port, String user, String password) throws IOException {
        return connect(host, port, user, password, -1);
    }

    /**
     * 与ftp服务器建立连接
     *
     * @param host     ftp服务器主机
     * @param user     用户名
     * @param password 密码
     * @param timeout  超时时间
     * @return
     */
    public boolean connect(String host, String user, String password, int timeout) throws IOException {
        return connect(host, -1, user, password, timeout);
    }

    /**
     * 与ftp服务器建立连接 (考虑如果连接不成功，尝试多连接几次)
     *
     * @param host     ftp服务器主机
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @param timeout  超时时间
     * @return
     */
    public boolean connect(String host, int port, String user, String password, int timeout) throws IOException {
        // addProtocolCommandListener(new PrintCommandListener(
        // new PrintWriter(System.out)));
        // boolean returnValue = false;
        try {
            // 连接到ftp服务器
            logger.info(" begin connect " + host + ":" + port);
            if (port != -1) {
                connect(host, port);
            } else {
                connect(host);
            }
            logger.info("Connected to " + host + ".");

            if (!FTPReply.isPositiveCompletion(getReplyCode())) {
                disconnect();
                logger.info("FTP server refused connection.");
                return false;
            }
            // 登陆到ftp服务器
            if (!login(user, password)) {
                logger.info("FTP login failed.");
                logout();
                if (isConnected()) {
                    try {
                        disconnect();
                    } catch (IOException f) {
                        f.printStackTrace();
                    }
                }
                return false;
            }

            // 设置数据传输超时时间
            if (timeout != -1) {
                logger.info("Set timeout :" + timeout);
                setDataTimeout(timeout);
            }
            // 默认取为ftp被动传输模式
            enterLocalPassiveMode();
        } catch (IOException f) {
            logger.error("", f);
            throw f;
        }
        return true;
    }

    /**
     * 上传单个文件到ftp服务器指定目录下
     *
     * @param localPath  文件在本地的绝对路径
     * @param localName  本地文件名
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param remoteName ftp服务器上的文件名
     * @param asc        是否以ASCII 方式上传
     * @return 是否上传成功
     */
    public boolean upload(String localPath, String localName, String remotePath, String remoteName, boolean asc)
            throws IOException {
        boolean returnValue = true;
        String localFileName = formatFilePath(localPath) + localName;
        try {
            if (!asc) {
                setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
            }
            InputStream input = new FileInputStream(localFileName);
            remotePath = handleRemotePath(remotePath);
            if (remotePath == null) {
                return false;
            }
            returnValue = storeFile(remotePath + remoteName, input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
            throw e;
        }
        return returnValue;
    }

    /**
     * 上传单个文件到ftp服务器指定目录下
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param remoteName ftp服务器上的文件名
     * @param asc        是否以ASCII 方式上传
     * @param input      InputStream
     */
    public void upload(String remotePath, String remoteName, boolean asc, InputStream input) throws IOException {
        try {
            if (!asc) {
                setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
            }
            remotePath = handleRemotePath(remotePath);
            if (remotePath == null) {
                throw new IOException("ftp remote path is null");
            }
            String fileName = new String((remotePath + remoteName).getBytes("UTF-8"), "iso-8859-1");
            boolean result = storeFile(fileName, input);
            if (!result) {
                throw new IOException("upload file failed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 批量上传文件到ftp服务器指定的目录路径上
     *
     * @param localPath  文件在本地的绝对路径
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @return
     */
    public boolean uploads(String localPath, String remotePath) throws IOException {
        File file = new File(localPath);
        File[] subFiles = file.listFiles(); // 得到目录下所有的文件
        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].isFile()) { // 只处理普通文件，不处理目录
                boolean asc = this.getFileTransferType(subFiles[i].getName());
                boolean flag = upload(localPath, subFiles[i].getName(), remotePath, subFiles[i].getName(), asc);
                if (!flag) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 批量上传文件到ftp服务器指定的目录路径上
     *
     * @param localPath  文件在本地的绝对路径
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param asc        文件传输方式
     * @return
     */
    public boolean uploads(String localPath, String remotePath, boolean asc) throws IOException {
        File file = new File(localPath);
        File[] subFiles = file.listFiles(); // 得到目录下所有的文件
        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].isFile()) { // 只处理普通文件，不处理目录
                boolean flag = upload(localPath, subFiles[i].getName(), remotePath, subFiles[i].getName(), asc);
                if (!flag) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 下载单个文件到指定的目录下
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param remoteName ftp服务器上的文件名
     * @param localPath  文件在本地的绝对路径
     * @param localName  本地文件名
     * @param asc        是否以ACSII方式传输
     * @return 是否下载成功
     */
    public boolean download(String remotePath, String remoteName, String localPath, String localName, boolean asc)
            throws IOException {
        boolean returnValue = true;
        localPath = createPath(localPath);
        try {
            if (!asc) {
                setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
            }
            OutputStream output = new FileOutputStream(localPath + localName);
            returnValue = retrieveFile(formatFTPPath(remotePath) + remoteName, output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
            throw e;
        }
        return returnValue;
    }

    /**
     * 下载单个文件到指定的目录下
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param remoteName ftp服务器上的文件名
     * @param asc        是否以ACSII方式传输
     * @param output     OutputStream
     */
    public void download(String remotePath, String remoteName, boolean asc, OutputStream output) throws IOException {
        try {
            if (!asc) {
                setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
            }
            boolean result = retrieveFile(formatFTPPath(remotePath) + remoteName, output);
            if (!result) {
                throw new IOException("download file failed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 批量下载文件到指定的目录路径上
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param localPath  文件在本地的绝对路径
     * @return
     */
    public boolean downloads(String remotePath, String localPath) throws IOException {
        boolean returnValue = true;
        try {
            FTPFile[] subFiles = listFiles(remotePath);
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isFile()) { //
                    String remoteFileName = subFiles[i].getName();
                    String localFileName = new String(subFiles[i].getName().getBytes("iso-8859-1"), "GBK");
                    boolean asc = getFileTransferType(localFileName);
                    returnValue = download(formatFTPPath(remotePath), remoteFileName, formatFilePath(localPath), localFileName, asc);
                    if (!returnValue) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
            throw e;
        }
        return returnValue;
    }

    /**
     * 批量下载文件到指定的目录路径上
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @param localPath  文件在本地的绝对路径
     * @param asc        文件传输方式
     * @return
     */
    public boolean downloads(String remotePath, String localPath, boolean asc) throws IOException {
        boolean returnValue = true;
        try {
            FTPFile[] subFiles = listFiles(remotePath);
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isFile()) { // 只处理一级目录下的文件
                    returnValue = download(formatFTPPath(remotePath), subFiles[i].getName(), formatFilePath(localPath),
                            subFiles[i].getName(), asc);
                    if (!returnValue) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
            throw e;
        }
        return returnValue;
    }

    /**
     * 处理上传时的ftp服务器的目录，当不存在该目录时即创建该目录
     *
     * @param remote
     */
    private String handleRemotePath(String remote) throws IOException {
        String remotePath = remote;
        try {
            if (!remote.endsWith(FTP_SEPARATOR)) {
                remotePath += FTP_SEPARATOR;
            }
            String[] names = listNames(remote);
            if (names != null && names.length > 0) {
                return remotePath;
            }
            if (remote.indexOf(FTP_SEPARATOR) == 0) {
                if (!changeWorkingDirectory(FTP_SEPARATOR)) {
                    System.out.println("改变工作目录到根目录时出现错误");
                    return null;
                }
            }

            StringTokenizer tok = new StringTokenizer(remote, "/");
            while (tok.hasMoreTokens()) {
                String subPath = tok.nextToken();
                if (!subPath.equals("")) {
                    makeDirectory(subPath);
                    if (!changeWorkingDirectory(subPath)) {
                        System.out.println("改变工作目录到:" + subPath + " 时出现错误");
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("the error is:" + e.getMessage());
            throw e;
        }
        if (remotePath.indexOf(FTP_SEPARATOR) == 0) {
            remotePath = FTP_SEPARATOR + remotePath;
        }
        return remotePath;
    }

    /**
     * 新建目录
     *
     * @param path 路径名
     * @return 格式处理后的目录
     */
    private String createPath(String path) {
        // 得到当前系统的文件分隔符
        String separator = File.separator;

        String filePath = formatFilePath(path);
        int begin = filePath.indexOf(separator);
        if (begin == 0) { // 考虑Unix，linux的情况，绝对路径往往以文件分隔符开始
            begin = filePath.indexOf(separator, 1);
        }

        // 循环进行目录的创建
        String retainPath = filePath;
        while (retainPath.indexOf(separator) > 0) {
            String subPath = filePath.substring(0, begin + 1);
            File subFilePath = new File(subPath);
            if (!subFilePath.exists()) {
                if (subFilePath.mkdir()) {
                    System.out.println("新建目录操作成功");
                } else {
                    System.out.println("新建目录操作出错");
                    return null;
                }
            }
            retainPath = filePath.substring(begin + 1);
            begin = begin + retainPath.indexOf(separator) + 1;
        }
        return filePath;
    }

    /**
     * 格式化传入的文件路径串
     *
     * @param path 路径名
     * @return 格式化后的文件路径串
     */
    public String formatFilePath(String path) {
        return formatFilePath(path, false);
    }

    /**
     * 格式化传入的文件路径串
     *
     * @param path   路径名
     * @param isFile 是否是文件
     * @return 格式化后的文件路径串
     */
    public String formatFilePath(String path, boolean isFile) {
        // 得到当前系统的文件分隔符
        String separator = File.separator;

        // 以当前操作系统的格式格式化传入的路径
        File file = new File(path);
        String filePath = file.toString();

        // 保证目录都已文件分隔符结束
        if (!isFile && !filePath.endsWith(separator)) {
            filePath = filePath + separator;
        }
        return filePath;
    }

    /**
     * 列出指定目录下所有的文件名,如果参数为某个文件,则如果该文件不存在,返回null
     *
     * @param pathname 路径或者文件名
     * @return
     * @throws IOException
     */
    public String[] listNames(String pathname) throws IOException {
        return super.listNames(pathname);
    }

    /**
     * 列出指定目录下所有的文件名
     *
     * @return 当前目录下所有的文件名
     * @throws IOException
     */
    public String[] listNames() throws IOException {
        return super.listNames();
    }

    /**
     * 删除ftp server上的文件
     *
     * @param pathname 文件名
     * @return
     * @throws IOException
     */
    public boolean deleteFile(String pathname) throws IOException {
        return super.deleteFile(pathname);
    }

    /**
     * 批量删除指定的目录路径上的文件
     *
     * @param remotePath 文件在ftp服务器上的绝对路径
     * @return
     */
    public boolean deletes(String remotePath) throws IOException {
        boolean returnValue = true;
        try {
            FTPFile[] subFiles = listFiles(remotePath);
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isFile()) { //
                    String remoteFileName = subFiles[i].getName();
                    deleteFile(remoteFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
            throw e;
        }
        return returnValue;
    }

    /**
     * 与ftp server断开连接
     *
     * @return
     * @throws IOException
     */
    public void disconnect() throws IOException {
        super.disconnect();
    }

    /**
     * 设置为本地主动模式
     */
    public void enterLocalActiveMode() {
        super.enterLocalActiveMode();
    }

    /**
     * 设置为本地被动模式
     */
    public void enterLocalPassiveMode() {
        super.enterLocalPassiveMode();
    }

    /**
     * 删除ftp server上的目录及目录下的文件(注意目录下不能有子目录)
     *
     * @param pathname 目录名
     * @return
     * @throws IOException
     */
    public boolean removeDirectory(String pathname) throws IOException {
        FTPFile[] subFiles = listFiles(pathname);
        for (int i = 0; i < subFiles.length; i++) {

            if (subFiles[i].isFile()) { // 只处理一级目录下的文件
                String _absPath = "";
                if (pathname.endsWith("/")) {
                    _absPath = pathname + subFiles[i].getName();
                } else {
                    _absPath = pathname + "/" + subFiles[i].getName();
                }
                boolean result = super.deleteFile(_absPath);
                if (!result) {
                    throw new IOException("删除目录" + pathname + "下的文件时出现异常!");
                }
            } else {
                System.out.println(subFiles[i].getName());
                throw new IOException("该目录下存在子目录,不允许删除!");
            }
        }
        return super.removeDirectory(pathname);
    }

    /**
     * 在ftp server上创建一个目录
     *
     * @param dir 创建的目录名
     */
    public boolean makeDirectory(String dir) throws IOException {
        try {
            return super.makeDirectory(dir);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 在ftp服务器上创建目录
     *
     * @param dir    要进入的目录
     * @param subdir 要创建的目录
     * @return boolean 是否成功
     * @throws IOException 抛出此异常。
     */
    public boolean makeDirectory(String dir, String subdir) throws IOException {
        try {
            boolean flag = super.changeWorkingDirectory(dir);
            if (!flag) {
                return flag;
            }
            return super.makeDirectory(subdir);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 修改ftp server上的目录或者文件名
     *
     * @param oldDir 旧的文件名
     * @param newDir 新的文件名
     * @return
     * @throws IOException
     */
    public boolean rename(String oldDir, String newDir) throws IOException {
        try {
            return super.rename(oldDir, newDir);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 列出文件大小信息。
     *
     * @param fileName FTP目录名（绝对路径，如"/aaa/bbb"，最后一个字符不允许是'/'）。
     * @return 文件大小。
     */
    public long getFileSize(String fileName) throws IOException {
        try {
            FTPFile[] subFiles = listFiles(fileName);
            if (subFiles != null) {
                return subFiles[0].getSize();
            } else {
                throw new IOException("没有找到指定的目录");
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 格式化ftp路径
     *
     * @param path 路径
     * @return
     */
    public String formatFTPPath(String path) {
        String filePath = path;
        if (!path.endsWith(FTP_SEPARATOR)) {
            filePath = filePath + FTP_SEPARATOR;
        }
        return filePath;
    }

    /**
     * 批量上传时，根据文件的后缀和配置文件来得到传输的方式
     *
     * @param fileName
     * @return true为ACSII方式，false为IMAGE方式
     */
    private boolean getFileTransferType(String fileName) {
        int dotAt = fileName.lastIndexOf(DOT_SUFFIX);
        if (dotAt < 0) { // 如果传入的文件没有后缀则默认为ACSII方式
            return true;
        } else {
            return false;
        }

    }

    /**
     * 注销本次连接
     */
    public boolean logout() throws IOException {
        return super.logout();
    }

    /**
     * 获取本次FTP指令执行结果代码
     */
    public int getReplyCode() {
        return super.getReplyCode();
    }

    public static void main(String args[]) {
        FTPClientDecorator ftpClientDecorator = new FTPClientDecorator(false);
        try {
            if (ftpClientDecorator.connect("10.40.93.152", "zxin10", "zxin10")) {

                if (ftpClientDecorator.removeDirectory("log")) {
                    System.out.println("success");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
