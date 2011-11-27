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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class EPubTest {

    @Test
    public void test() throws IOException {
        String file = getClass().getResource("pr_neo_0001_sternenstaub_borsch_frank.epub").getFile();
        EPub testee = new EPub(file);
        List<Chapter> chapters = testee.getChapters();
        Chapter chapter1 = chapters.get(3);
        assertEquals("1.", chapter1.getTitle());
        String text1 = chapter1.getText();
        assertTrue(text1.startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\""
                + " \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"));
    }
}
