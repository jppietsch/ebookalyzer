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

import static de.jppietsch.epub.Zip.NCX_NS;

import org.w3c.dom.Element;

public final class Chapter {

    private final String label;
    private final String text;

    Chapter(Zip zip, Element navPoint, String prefix) {
        label = navPoint.getElementsByTagNameNS(NCX_NS, "navLabel").item(0).getTextContent().trim();
        String src =
                prefix
                        + navPoint.getElementsByTagNameNS(NCX_NS, "content").item(0).getAttributes()
                                .getNamedItem("src").getNodeValue();

        String fullText = zip.string(src);
        text =
                fullText.substring(fullText.indexOf("<html")).replace(
                        "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=utf-8\"/>", "");
    }

    public String getTitle() {
        return label;
    }

    public String getText() {
        return text;
    }

    public boolean search(String word) {
        return text.indexOf(word) >= 0;
    }

}
