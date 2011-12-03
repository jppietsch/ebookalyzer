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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public final class EPub {

    private final List<Chapter> chapters = new ArrayList<Chapter>();
    private String title;

    public EPub(String name) throws IOException {
        this(new Zip(name));
    }

    public EPub(File file) throws IOException {
        this(new Zip(file));
    }

    private EPub(Zip zip) {
        try {
            String rootfile = analyseContainer(zip, "META-INF/container.xml");
            String prefix = rootfile.substring(0, rootfile.lastIndexOf('/') + 1);
            String toc = analyseContent(zip, rootfile);
            analyseToc(zip, prefix + toc, prefix);
        } finally {
            zip.close();
        }
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public String getTitle() {
        return title;
    }

    public int search(String word) {
        int chapterIndex = 0;
        for (Chapter chapter: chapters) {
            int index = chapter.search(word);
            if (index >= 0) {
                return chapterIndex;
            }
            chapterIndex++;
        }
        return -1;
    }

    private static String analyseContainer(Zip file, String name) {
        Document document = file.document(name);
        NodeList rootfiles =
                document.getElementsByTagNameNS("urn:oasis:names:tc:opendocument:xmlns:container", "rootfile");
        return rootfiles.item(0).getAttributes().getNamedItem("full-path").getNodeValue();
    }

    private String analyseContent(Zip file, String name) {
        Document document = file.document(name);
        String namespace = "http://www.idpf.org/2007/opf";
        NodeList spines = document.getElementsByTagNameNS(namespace, "spine");
        String toc = spines.item(0).getAttributes().getNamedItem("toc").getNodeValue();

        NodeList titles = document.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
        title = titles.item(0).getTextContent();

        NodeList items = document.getElementsByTagNameNS(namespace, "item");
        for (int i = 0; i < items.getLength(); i++) {
            NamedNodeMap attributes = items.item(i).getAttributes();
            if (toc.equals(attributes.getNamedItem("id").getNodeValue())) {
                return attributes.getNamedItem("href").getNodeValue();
            }
        }

        throw new RuntimeException("no toc found");
    }

    private void analyseToc(Zip zip, String name, String prefix) {
        Document document = zip.document(name);
        NodeList navPoints = document.getElementsByTagNameNS(Zip.NCX_NS, "navPoint");
        for (int i = 0; i < navPoints.getLength(); i++) {
            chapters.add(new Chapter(zip, (Element) navPoints.item(i), prefix));
        }
    }

}
