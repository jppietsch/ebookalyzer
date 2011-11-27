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
package de.jppietsch.ebookalyzer.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Entity implements Serializable {

    private static final long serialVersionUID = -5278747678487301971L;

    private final String primaryName;

    private final Set<String> secondaryNames = new HashSet<String>();

    public Entity(String primaryName) {
        this.primaryName = primaryName;
    }

    @Override
    public String toString() {
        return primaryName;
    }

    public List<TextSegment> findAllSegments(String text) {
        List<TextSegment> result = new ArrayList<TextSegment>();

        findAllSegments(text, primaryName, result);
        for (String secondaryName: secondaryNames) {
            findAllSegments(text, secondaryName, result);
        }

        return result;
    }

    public void addSecondaryName(String secondaryName) {
        secondaryNames.add(secondaryName);
    }

    public void removeSecondaryName(String secondaryName) {
        secondaryNames.remove(secondaryName);
    }

    private void findAllSegments(String text, String name, List<TextSegment> result) {
        int index = text.indexOf(name);
        int length = name.length();

        while (index >= 0) {
            result.add(new TextSegment(index, length));
            index = text.indexOf(name, index + name.length());
        }
    }

}
