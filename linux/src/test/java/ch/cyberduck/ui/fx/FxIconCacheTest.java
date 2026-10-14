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

import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javafx.scene.image.Image;

public class FxIconCacheTest {

    // A PNG of one transparent pixel
    private static final String PIXEL = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    @Before
    public void toolkit() {
        FxToolkit.init();
    }

    @Test
    public void testBundledIconByMacOSName() {
        final FxIconCache cache = new FxIconCache();
        final Image image = cache.iconNamed("ftp.tiff", 32);
        assertNotNull(image);
        assertFalse(image.isError());
        assertEquals(64, image.getWidth(), 0);
        assertSame(image, cache.iconNamed("ftp.tiff", 32));
    }

    @Test
    public void testMissingIcon() {
        assertNull(new FxIconCache().iconNamed("smb.tiff", 32));
    }

    @Test
    public void testUnreadableIcon() {
        assertNull(new FxIconCache().iconNamed("broken.tiff", 32));
    }

    @Test
    public void testIconOfProfile() {
        final FxIconCache cache = new FxIconCache();
        assertNotNull(cache.iconNamed(PIXEL, 32));
        assertNull(cache.iconNamed(Base64.getEncoder().encodeToString("not a png".getBytes()), 32));
    }
}
