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
package de.jppietsch.ebookalyzer.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.jppietsch.ebookalyzer.entity.Entity;

public class Dictionary implements ListModel {

    private final List<Entity> entities = new ArrayList<Entity>();

    private final EventListenerList listeners = new EventListenerList();

    @Override
    public int getSize() {
        return entities.size();
    }

    @Override
    public Entity getElementAt(int index) {
        return entities.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        listeners.add(ListDataListener.class, listener);
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        listeners.remove(ListDataListener.class, listener);
    }

    public void add(String currentSelection) {
        entities.add(new Entity(currentSelection));
        for (ListDataListener listener: listeners.getListeners(ListDataListener.class)) {
            int index = entities.size() - 1;
            listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, entities.size() - 1));
        }
    }

}
