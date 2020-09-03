package com.papenko.filestorage.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties("formats")
public class FileExtensionProperties {

    /**
     * Map<FileFormat,ExtensionList>
     */
    private Map<String, Set<String>> extensions;

    public Map<String, Set<String>> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Set<String>> extensions) {
        this.extensions = extensions;
    }
}
