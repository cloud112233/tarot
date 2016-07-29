package com.myee.tarot.web.files.vo;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FileItem implements Serializable{
    private static final int DIR  = 0;
    private static final int FILE = 1;

    private String name;
    private String path;
    private String salt;
    private String content;
    private long   modified;
    private Long   size;
    private int    type;

    private List<FileItem> children;

    public FileItem() {
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String absPath) {
        this.path = absPath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<FileItem> getChildren() {
        return children;
    }

    public void setChildren(List<FileItem> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static FileItem toResourceModel(File file) {
        FileItem resVo = new FileItem();
        resVo.setName(file.getName());
        resVo.setPath(file.getAbsolutePath());
        resVo.setModified(file.lastModified());
        resVo.type = file.isDirectory() ? DIR : FILE;
        resVo.setSize(file.length());
        return resVo;
    }

}
