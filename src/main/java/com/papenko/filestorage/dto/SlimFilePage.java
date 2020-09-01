package com.papenko.filestorage.dto;

import com.papenko.filestorage.entity.File;

import java.util.List;

public class SlimFilePage {
    private final long total;
    private final List<File> page;

    public SlimFilePage(long total, List<File> page) {
        this.total = total;
        this.page = page;
    }

    public long getTotal() {
        return total;
    }

    public List<File> getPage() {
        return page;
    }
}
