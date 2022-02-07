/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.maxgamer.quickshop.util.security;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>Taken from the jarsigner utility.
 * <p>
 * see https://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/sun/security/tools/jarsigner/Main.java#l570
 *
 * @author Roland Schemers
 * @author Jan Luehe
 */
public class JarVerifyTool {
    @NotNull
    public List<JarEntry> verify(@NotNull JarFile jar) throws IOException {
        List<JarEntry> modified = new ArrayList<>();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            try (InputStream is = jar.getInputStream(entry)) {
                byte[] buffer = new byte[8192];
                //noinspection StatementWithEmptyBody
                while ((is.read(buffer, 0, buffer.length)) != -1) {
                    // We just read. This will throw a SecurityException
                    // if a signature/digest check fails.
                }
            } catch (SecurityException se) {
                modified.add(entry);
            }
        }
        return modified;
    }
}
