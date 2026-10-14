package ch.cyberduck.ui.fx;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.resources.IconCache;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;

/**
 * Images by name, such as the icons of protocols. The name is either a filename, as in the macOS application where the
 * files are .tiff, or the image itself as Base64 text, as profiles of the vendors have it. Folders and files are drawn
 * as shapes in {@link Icons}, so there are no images for them.
 */
public class FxIconCache implements IconCache<Image> {
    private static final Logger log = LogManager.getLogger(FxIconCache.class);

    private final Map<String, Optional<Image>> cache = new ConcurrentHashMap<>();

    /**
     * @return Image scaled to the size or null when there is none with the name
     */
    @Override
    public Image iconNamed(final String name, final Integer width, final Integer height) {
        if(StringUtils.isBlank(name)) {
            return null;
        }
        final int w = null == width ? 0 : width;
        final int h = null == height ? 0 : height;
        return cache.computeIfAbsent(String.format("%s@%dx%d", name, w, h), key -> Optional.ofNullable(this.load(name, w, h))).orElse(null);
    }

    private Image load(final String name, final int width, final int height) {
        // Twice the size keeps the image sharp on high resolution displays
        final double w = width * 2;
        final double h = height * 2;
        try {
            if(name.length() < 64 && name.contains(".")) {
                // The extension of the macOS application is replaced by the one JavaFX reads
                final String resource = String.format("/img/%s.png", StringUtils.substringBeforeLast(name, "."));
                try(InputStream in = FxIconCache.class.getResourceAsStream(resource)) {
                    if(null == in) {
                        log.debug("No icon {} for {}", resource, name);
                        return null;
                    }
                    final Image image = new Image(in, w, h, true, true);
                    if(image.isError()) {
                        log.warn("Failure reading icon {}", resource, image.getException());
                        return null;
                    }
                    return image;
                }
            }
            try(InputStream in = new ByteArrayInputStream(Base64.getMimeDecoder().decode(name))) {
                final Image image = new Image(in, w, h, true, true);
                return image.isError() ? null : image;
            }
        }
        catch(Exception e) {
            log.warn("Failure loading icon {}", StringUtils.abbreviate(name, 32), e);
            return null;
        }
    }

    @Override
    public Image documentIcon(final String extension, final Integer size) {
        return null;
    }

    @Override
    public Image documentIcon(final String extension, final Integer size, final Image badge) {
        return null;
    }

    @Override
    public Image folderIcon(final Integer size) {
        return null;
    }

    @Override
    public Image folderIcon(final Integer size, final Image badge) {
        return null;
    }

    @Override
    public Image fileIcon(final Path item, final Integer size) {
        return null;
    }

    @Override
    public Image aliasIcon(final String extension, final Integer size) {
        return null;
    }

    @Override
    public Image applicationIcon(final Application app, final Integer size) {
        return null;
    }
}
