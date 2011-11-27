// Copyright © 2011 Jan-Peter Pietsch
//
// This file is part of Ebookalyzer.
//
// Ebookalyzer is free software: you can redistribute it and/or modify it under the terms of
// the GNU General Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later version.
//
// Ebookalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with Ebookalyzer.
// If not, see <http://www.gnu.org/licenses/>.
package de.jppietsch.epub;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

final class Zip {

    static final String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";

    private final ZipFile file;

    private final DocumentBuilder builder;

    Zip(String name) throws IOException {
        this(new ZipFile(name));
    }

    public Zip(File file) throws IOException {
        this(new ZipFile(file));
    }

    private Zip(ZipFile file) {
        this.file = file;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    Document document(String name) {
        ZipEntry entry = file.getEntry(name);
        try {
            return builder.parse(file.getInputStream(entry));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String string(String name) {
        ZipEntry entry = file.getEntry(name);
        byte[] buffer = new byte[(int) entry.getSize()];
        try {
            InputStream stream = file.getInputStream(entry);
            int offset = 0;
            int count = stream.read(buffer);
            while (count > 0) {
                offset += count;
                count = stream.read(buffer, offset, buffer.length - offset);
            }
            return new String(buffer, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void close() {
        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
