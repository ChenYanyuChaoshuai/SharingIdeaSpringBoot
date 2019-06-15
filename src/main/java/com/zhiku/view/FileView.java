package com.zhiku.view;

import com.zhiku.entity.File;
import com.zhiku.entity.FileKeys;

public class FileView extends File {
    private String upperName;

    private FileKeys fileKeys;

    public String getUpperName() {
        return upperName;
    }

    public void setUpperName(String upperName) {
        this.upperName = upperName;
    }

    public FileKeys getFileKeys() {
        return fileKeys;
    }

    public void setFileKeys(FileKeys fileKeys) {
        this.fileKeys = fileKeys;
    }
}
