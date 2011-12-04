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

import static java.awt.BorderLayout.EAST;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.ResourceBundle.getBundle;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.jppietsch.ebookalyzer.entity.Entity;
import de.jppietsch.ebookalyzer.entity.TextSegment;
import de.jppietsch.epub.EPub;

public final class EBookalyzerFrame extends JFrame {

    private static final String TITLE = "Ebookalyzer";

    private static final String ENTITIES_EXTENSION = "entities";

    private final JTextPane chapterPane = new JTextPane();

    private Dictionary listModel = new Dictionary();

    private final JList list = new JList(listModel);

    private final ResourceBundle actionResources = getBundle("de.jppietsch.ebookalyzer.swing." + "ActionResources");

    private String currentSelection;

    private Entity selectedEntity;

    private EPub epub;

    private int page;

    private final FileNameExtensionFilter entityFilter = new FileNameExtensionFilter("Entitäten", ENTITIES_EXTENSION);

    private final Action createAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            add();
        }
    }, "createEntity");

    private Action deleteAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }, "deleteEntity");

    private final Action associateAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            associate();
        }
    }, "associate");

    private final Action dissociateAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dissociate();
        }
    }, "dissociate");

    private final Action firstAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            firstPage();
        }
    }, "firstPage");

    private final Action previousAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            previousPage();
        }
    }, "previousPage");

    private final Action nextAction = init(new AbstractAction(">>") {
        @Override
        public void actionPerformed(ActionEvent e) {
            nextPage();
        }
    }, "nextPage");

    private final Action lastAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            lastPage();
        }
    }, "lastPage");

    private final Action searchInBookAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchInBook();
        }
    }, "searchInBook");

    private final Action previousOccurenceAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchPreviousOccurence();
        }
    }, "previousOccurence");

    private final Action nextOccurenceAction = init(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchNextOccurence();
        }
    }, "nextOccurence");

    public EBookalyzerFrame() {
        super(TITLE);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initToolBar();

        chapterPane.setContentType("text/html");
        chapterPane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                onSelectionChange(e);
            }
        });
        add(new JScrollPane(chapterPane));
        list.setSelectionMode(SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectEntity(e);
            }

        });
        add(new JScrollPane(list), EAST);

        pack();
    }

    void add() {
        listModel.add(currentSelection);
    }

    void associate() {
        selectedEntity.addSecondaryName(currentSelection);
    }

    void dissociate() {
        selectedEntity.removeSecondaryName(currentSelection);
    }

    void open() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("elektronische Bücher", "epub");
        chooser.setFileFilter(filter);
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                epub = new EPub(chooser.getSelectedFile());
                page = 0;
                setTitle(TITLE + " - " + epub.getTitle());
                displayPage();
                searchInBookAction.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void load() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(entityFilter);
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream stream = new FileInputStream(chooser.getSelectedFile());
                try {
                    ObjectInputStream oos = new ObjectInputStream(stream);
                    listModel = (Dictionary) oos.readObject();
                    list.setModel(listModel);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void save() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(entityFilter);
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (!selectedFile.getName().endsWith("." + ENTITIES_EXTENSION)) {
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + ENTITIES_EXTENSION);
            }
            try {
                FileOutputStream stream = new FileOutputStream(selectedFile);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(stream);
                    oos.writeObject(listModel);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void onSelectionChange(CaretEvent e) {
        int dot = e.getDot();
        int mark = e.getMark();
        boolean selected = dot != mark;

        createAction.setEnabled(selected);
        if (selected) {
            int offset = min(dot, mark);
            int length = abs(dot - mark);
            try {
                currentSelection = chapterPane.getDocument().getText(offset, length);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        } else {
            currentSelection = null;
        }
        boolean full = currentSelection != null && selectedEntity != null;
        associateAction.setEnabled(full);
        dissociateAction.setEnabled(full);
    }

    void selectEntity(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            StyledDocument document = (StyledDocument) chapterPane.getDocument();
            highlight(document, false);
            int selectedIndex = list.getSelectedIndex();
            selectedEntity = selectedIndex < 0 ? null : listModel.getElementAt(selectedIndex);
            highlight(document, true);
            boolean selected = selectedEntity != null;
            deleteAction.setEnabled(selected);
            previousOccurenceAction.setEnabled(selected);
            nextOccurenceAction.setEnabled(selected);
            boolean full = currentSelection != null && selectedEntity != null;
            associateAction.setEnabled(full);
            dissociateAction.setEnabled(full);
        }
    }

    void highlight(StyledDocument document, boolean on) {
        Color color = on ? Color.YELLOW : Color.WHITE;
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setBackground(attributes, color);
        String text;
        try {
            text = document.getText(0, document.getLength());
            if (selectedEntity != null) {
                for (TextSegment segment: selectedEntity.findAllSegments(text)) {
                    document.setCharacterAttributes(segment.getOffset(), segment.getLength(), attributes, false);
                }
            }
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    void firstPage() {
        page = 0;
        displayPage();
    }

    void previousPage() {
        page--;
        displayPage();
    }

    void nextPage() {
        page++;
        displayPage();
    }

    void lastPage() {
        page = epub.getChapters().size() - 1;
        displayPage();
    }

    void searchInBook() {
        String word = JOptionPane.showInputDialog(this, "Suche");
        int chapterIndex = epub.search(word);
        if (chapterIndex >= 0) {
            page = chapterIndex;
            displayPage();
            StyledDocument styledDocument = chapterPane.getStyledDocument();
            try {
                int offset = styledDocument.getText(0, styledDocument.getLength()).indexOf(word);
                select(offset, word);
            } catch (BadLocationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    void searchPreviousOccurence() {
        Pattern pattern = selectedEntity.getPattern();
        StyledDocument document = chapterPane.getStyledDocument();
        try {
            int caretPosition = chapterPane.getCaretPosition();
            Matcher matcher = pattern.matcher(document.getText(0, caretPosition - 1));
            int offset = -1;
            String token = null;
            while (matcher.find()) {
                offset = matcher.start();
                token = matcher.group();
            }
            if (token == null) {
                System.out.println("kein voriges Vorkommen gefunden im aktuellen Kapitel.");
            } else {
                System.out.println("voriges Vorkommen gefunden an Position " + offset);
                select(offset, token);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    void searchNextOccurence() {
        Pattern pattern = selectedEntity.getPattern();
        StyledDocument document = chapterPane.getStyledDocument();
        try {
            int caretPosition = chapterPane.getCaretPosition();
            Matcher matcher = pattern.matcher(document.getText(caretPosition, document.getLength() - caretPosition));
            if (matcher.find()) {
                System.out.println("weiteres Vorkommen gefunden an Position " + matcher.start());
                select(caretPosition + matcher.start(), matcher.group());
            } else {
                System.out.println("kein weiteres Vorkommen gefunden im aktuellen Kapitel.");
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void select(int offset, String word) {
        chapterPane.setSelectionStart(offset);
        chapterPane.setSelectionEnd(offset + word.length());
        chapterPane.requestFocusInWindow();
    }

    private void displayPage() {
        firstAction.setEnabled(true);
        previousAction.setEnabled(page > 0);
        nextAction.setEnabled(page < epub.getChapters().size() - 1);
        lastAction.setEnabled(true);

        chapterPane.setContentType("text/html");
        String text = epub.getChapters().get(page).getText();
        chapterPane.setText(text);
        highlight((StyledDocument) chapterPane.getDocument(), true);
        chapterPane.setCaretPosition(0);
    }

    private void initToolBar() {
        JToolBar toolbar = new JToolBar();

        toolbar.setFloatable(false);

        toolbar.add(init(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        }, "open"));

        toolbar.add(init(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        }, "load"));

        toolbar.add(init(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        }, "save"));

        toolbar.addSeparator();
        createAction.setEnabled(false);
        toolbar.add(createAction);
        deleteAction.setEnabled(false);
        toolbar.add(deleteAction);
        associateAction.setEnabled(false);
        toolbar.add(associateAction);
        dissociateAction.setEnabled(false);
        toolbar.add(dissociateAction);

        toolbar.addSeparator();
        firstAction.setEnabled(false);
        toolbar.add(firstAction);
        previousAction.setEnabled(false);
        toolbar.add(previousAction);
        nextAction.setEnabled(false);
        toolbar.add(nextAction);
        lastAction.setEnabled(false);
        toolbar.add(lastAction);

        toolbar.addSeparator();
        searchInBookAction.setEnabled(false);
        toolbar.add(searchInBookAction);
        previousOccurenceAction.setEnabled(false);
        toolbar.add(previousOccurenceAction);
        nextOccurenceAction.setEnabled(false);
        toolbar.add(nextOccurenceAction);

        add(toolbar, BorderLayout.NORTH);
    }

    private Action init(Action action, String key) {
        action.putValue(Action.NAME, actionResources.getString(key + ".name"));
        action.putValue(Action.LARGE_ICON_KEY,
                new ImageIcon(getClass().getResource(actionResources.getString(key + ".icon"))));
        action.putValue(Action.SHORT_DESCRIPTION, actionResources.getString(key + ".short"));
        return action;
    }

}
