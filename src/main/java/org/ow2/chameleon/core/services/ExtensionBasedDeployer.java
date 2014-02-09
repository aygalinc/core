package org.ow2.chameleon.core.services;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * A default implementation of the deployer accepting file by extension.
 */
public class ExtensionBasedDeployer extends AbstractDeployer {

    /**
     * The list of managed extensions (immutable).
     */
    private final List<String> extensions;

    public ExtensionBasedDeployer(String[] extensions) {
        this(Arrays.asList(extensions));
    }

    public ExtensionBasedDeployer(String extension) {
        this(new String[] {extension});
    }

    public ExtensionBasedDeployer(List<String> extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean accept(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return extensions.contains(extension);
    }
}