/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.main;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Class for create main menu and handle main menu events.
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindowMenu implements ActionListener {
    /** MainWindow instance. */
    protected final MainWindow mainWindow;

    public MainWindowMenu(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Code for dispatching events from components to event handlers.
     * 
     * @param evt
     *            event info
     */
    public void actionPerformed(ActionEvent evt) {
        // Item what perform event.
        JMenuItem menuItem = (JMenuItem) evt.getSource();

        // Get item name from actionCommand.
        String action = menuItem.getActionCommand();

        // Find method by item name.
        String methodName = action + "ActionPerformed";
        Method method;
        try {
            method = mainWindow.getClass().getMethod(methodName, ActionEvent.class);
        } catch (NoSuchMethodException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }

        // Call ...MenuItemActionPerformed method.
        try {
            method.invoke(mainWindow, evt);
        } catch (IllegalAccessException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        } catch (InvocationTargetException ex) {
            throw new IncompatibleClassChangeError("Error invoke method handler for main menu");
        }
    }

    /**
     * Initialize menu items.
     */
    JMenuBar initComponents() {
        mainMenu = new JMenuBar();
        projectMenu = createMenu("TF_MENU_FILE");
        editMenu = createMenu("TF_MENU_EDIT");
        gotoMenu = createMenu("MW_GOTOMENU");
        viewMenu = createMenu("MW_VIEW_MENU");
        toolsMenu = createMenu("TF_MENU_TOOLS");
        optionsMenu = createMenu("MW_OPTIONSMENU");
        helpMenu = createMenu("TF_MENU_HELP");

        projectExitMenuItem = new JMenuItem();
        projectNewMenuItem = new JMenuItem();
        projectOpenMenuItem = new JMenuItem();
        projectImportMenuItem = new JMenuItem();
        projectWikiImportMenuItem = new JMenuItem();
        projectReloadMenuItem = new JMenuItem();
        projectCloseMenuItem = new JMenuItem();
        projectSaveMenuItem = new JMenuItem();
        projectCompileMenuItem = new JMenuItem();
        projectEditMenuItem = new JMenuItem();
        viewFileListMenuItem = new JMenuItem();
        editUndoMenuItem = new JMenuItem();
        editRedoMenuItem = new JMenuItem();
        editOverwriteTranslationMenuItem = new JMenuItem();
        editInsertTranslationMenuItem = new JMenuItem();
        editOverwriteSourceMenuItem = new JMenuItem();
        editInsertSourceMenuItem = new JMenuItem();
        editFindInProjectMenuItem = new JMenuItem();
        switchCaseSubMenu = new JMenu();
        lowerCaseMenuItem = new JMenuItem();
        upperCaseMenuItem = new JMenuItem();
        titleCaseMenuItem = new JMenuItem();
        cycleSwitchCaseMenuItem = new JMenuItem();
        editSelectFuzzy1MenuItem = new JMenuItem();
        editSelectFuzzy2MenuItem = new JMenuItem();
        editSelectFuzzy3MenuItem = new JMenuItem();
        editSelectFuzzy4MenuItem = new JMenuItem();
        editSelectFuzzy5MenuItem = new JMenuItem();
        gotoNextUntranslatedMenuItem = new JMenuItem();
        gotoNextSegmentMenuItem = new JMenuItem();
        gotoPreviousSegmentMenuItem = new JMenuItem();
        gotoSegmentMenuItem = new JMenuItem();
        gotoHistoryForwardMenuItem = new JMenuItem();
        gotoHistoryBackMenuItem = new JMenuItem();
        viewMarkTranslatedSegmentsCheckBoxMenuItem = new JCheckBoxMenuItem();
        viewMarkUntranslatedSegmentsCheckBoxMenuItem = new JCheckBoxMenuItem();
        viewDisplaySegmentSourceCheckBoxMenuItem = new JCheckBoxMenuItem();
        toolsValidateTagsMenuItem = new JMenuItem();
        optionsTabAdvanceCheckBoxMenuItem = new JCheckBoxMenuItem();
        optionsAlwaysConfirmQuitCheckBoxMenuItem = new JCheckBoxMenuItem();
        optionsFontSelectionMenuItem = new JMenuItem();
        optionsSetupFileFiltersMenuItem = new JMenuItem();
        optionsSentsegMenuItem = new JMenuItem();
        optionsSpellCheckMenuItem = new JMenuItem();
        optionsWorkflowMenuItem = new JMenuItem();
        optionsRestoreGUIMenuItem = new JMenuItem();
        helpContentsMenuItem = new JMenuItem();
        helpAboutMenuItem = new JMenuItem();

        projectMenu.add(projectNewMenuItem = createMenuItem("TF_MENU_FILE_CREATE"));
        projectMenu.add(projectOpenMenuItem = createMenuItem("TF_MENU_FILE_OPEN"));
        projectMenu.add(projectImportMenuItem = createMenuItem("TF_MENU_FILE_IMPORT"));
        projectMenu.add(projectWikiImportMenuItem = createMenuItem("TF_MENU_WIKI_IMPORT"));
        projectMenu.add(projectReloadMenuItem = createMenuItem("TF_MENU_PROJECT_RELOAD"));
        projectMenu.add(projectCloseMenuItem = createMenuItem("TF_MENU_FILE_CLOSE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectSaveMenuItem = createMenuItem("TF_MENU_FILE_SAVE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectCompileMenuItem = createMenuItem("TF_MENU_FILE_COMPILE"));
        projectMenu.add(new JSeparator());
        projectMenu.add(projectEditMenuItem = createMenuItem("MW_PROJECTMENU_EDIT"));
        projectMenu.add(viewFileListMenuItem = createMenuItem("TF_MENU_FILE_PROJWIN"));
        projectExitMenuItem = createMenuItem("TF_MENU_FILE_QUIT");

        Mnemonics.setLocalizedText(editUndoMenuItem, OStrings.getString("TF_MENU_EDIT_UNDO"));
        editUndoMenuItem.addActionListener(this);

        editMenu.add(editUndoMenuItem);

        Mnemonics.setLocalizedText(editRedoMenuItem, OStrings.getString("TF_MENU_EDIT_REDO"));
        editRedoMenuItem.addActionListener(this);

        editMenu.add(editRedoMenuItem);

        editMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(editOverwriteTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_RECYCLE"));
        editOverwriteTranslationMenuItem.addActionListener(this);

        editMenu.add(editOverwriteTranslationMenuItem);

        Mnemonics.setLocalizedText(editInsertTranslationMenuItem, OStrings.getString("TF_MENU_EDIT_INSERT"));
        editInsertTranslationMenuItem.addActionListener(this);

        editMenu.add(editInsertTranslationMenuItem);

        editMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(editOverwriteSourceMenuItem, OStrings.getString("TF_MENU_EDIT_SOURCE_OVERWRITE"));
        editOverwriteSourceMenuItem.addActionListener(this);

        editMenu.add(editOverwriteSourceMenuItem);

        Mnemonics.setLocalizedText(editInsertSourceMenuItem, OStrings.getString("TF_MENU_EDIT_SOURCE_INSERT"));
        editInsertSourceMenuItem.addActionListener(this);

        editMenu.add(editInsertSourceMenuItem);

        editMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(editFindInProjectMenuItem, OStrings.getString("TF_MENU_EDIT_FIND"));
        editFindInProjectMenuItem.addActionListener(this);

        editMenu.add(editFindInProjectMenuItem);

        editMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(switchCaseSubMenu, OStrings.getString("TF_EDIT_MENU_SWITCH_CASE"));
        Mnemonics.setLocalizedText(lowerCaseMenuItem, OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_LOWER"));
        lowerCaseMenuItem.addActionListener(this);

        switchCaseSubMenu.add(lowerCaseMenuItem);

        Mnemonics.setLocalizedText(upperCaseMenuItem, OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_UPPER"));
        upperCaseMenuItem.addActionListener(this);

        switchCaseSubMenu.add(upperCaseMenuItem);

        Mnemonics.setLocalizedText(titleCaseMenuItem, OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_TO_TITLE"));
        titleCaseMenuItem.addActionListener(this);

        switchCaseSubMenu.add(titleCaseMenuItem);

        switchCaseSubMenu.add(new JSeparator());

        cycleSwitchCaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK));
        Mnemonics.setLocalizedText(cycleSwitchCaseMenuItem, OStrings.getString("TF_EDIT_MENU_SWITCH_CASE_CYCLE"));
        cycleSwitchCaseMenuItem.addActionListener(this);

        switchCaseSubMenu.add(cycleSwitchCaseMenuItem);

        editMenu.add(switchCaseSubMenu);

        editMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(editSelectFuzzy1MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_1"));
        editSelectFuzzy1MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy1MenuItem);

        Mnemonics.setLocalizedText(editSelectFuzzy2MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_2"));
        editSelectFuzzy2MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy2MenuItem);

        Mnemonics.setLocalizedText(editSelectFuzzy3MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_3"));
        editSelectFuzzy3MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy3MenuItem);

        Mnemonics.setLocalizedText(editSelectFuzzy4MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_4"));
        editSelectFuzzy4MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy4MenuItem);

        Mnemonics.setLocalizedText(editSelectFuzzy5MenuItem, OStrings.getString("TF_MENU_EDIT_COMPARE_5"));
        editSelectFuzzy5MenuItem.addActionListener(this);

        editMenu.add(editSelectFuzzy5MenuItem);

        Mnemonics.setLocalizedText(gotoNextUntranslatedMenuItem, OStrings.getString("TF_MENU_EDIT_UNTRANS"));
        gotoNextUntranslatedMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextUntranslatedMenuItem);

        Mnemonics.setLocalizedText(gotoNextSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_NEXT"));
        gotoNextSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoNextSegmentMenuItem);

        Mnemonics.setLocalizedText(gotoPreviousSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_PREV"));
        gotoPreviousSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoPreviousSegmentMenuItem);

        Mnemonics.setLocalizedText(gotoSegmentMenuItem, OStrings.getString("TF_MENU_EDIT_GOTO"));
        gotoSegmentMenuItem.addActionListener(this);

        gotoMenu.add(gotoSegmentMenuItem);

        gotoMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(gotoHistoryForwardMenuItem, OStrings.getString("TF_MENU_GOTO_FORWARD_IN_HISTORY"));
        gotoHistoryForwardMenuItem.addActionListener(this);

        gotoMenu.add(gotoHistoryForwardMenuItem);

        Mnemonics.setLocalizedText(gotoHistoryBackMenuItem, OStrings.getString("TF_MENU_GOTO_BACK_IN_HISTORY"));
        gotoHistoryBackMenuItem.addActionListener(this);

        gotoMenu.add(gotoHistoryBackMenuItem);

        Mnemonics.setLocalizedText(viewMarkTranslatedSegmentsCheckBoxMenuItem, OStrings
                .getString("TF_MENU_DISPLAY_MARK_TRANSLATED"));
        viewMarkTranslatedSegmentsCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewMarkTranslatedSegmentsCheckBoxMenuItem);

        Mnemonics.setLocalizedText(viewMarkUntranslatedSegmentsCheckBoxMenuItem, ResourceBundle.getBundle(
                "org/omegat/Bundle").getString("TF_MENU_DISPLAY_MARK_UNTRANSLATED"));
        viewMarkUntranslatedSegmentsCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewMarkUntranslatedSegmentsCheckBoxMenuItem);

        Mnemonics.setLocalizedText(viewDisplaySegmentSourceCheckBoxMenuItem, OStrings
                .getString("MW_VIEW_MENU_DISPLAY_SEGMENT_SOURCES"));
        viewDisplaySegmentSourceCheckBoxMenuItem.addActionListener(this);

        viewMenu.add(viewDisplaySegmentSourceCheckBoxMenuItem);

        Mnemonics.setLocalizedText(toolsValidateTagsMenuItem, OStrings.getString("TF_MENU_TOOLS_VALIDATE"));
        toolsValidateTagsMenuItem.addActionListener(this);

        toolsMenu.add(toolsValidateTagsMenuItem);

        Mnemonics.setLocalizedText(optionsTabAdvanceCheckBoxMenuItem, OStrings.getString("TF_MENU_DISPLAY_ADVANCE"));
        optionsTabAdvanceCheckBoxMenuItem.addActionListener(this);

        optionsMenu.add(optionsTabAdvanceCheckBoxMenuItem);

        Mnemonics.setLocalizedText(optionsAlwaysConfirmQuitCheckBoxMenuItem, OStrings
                .getString("MW_OPTIONSMENU_ALWAYS_CONFIRM_QUIT"));
        optionsAlwaysConfirmQuitCheckBoxMenuItem.addActionListener(this);

        optionsMenu.add(optionsAlwaysConfirmQuitCheckBoxMenuItem);

        optionsMenu.add(new JSeparator());

        Mnemonics.setLocalizedText(optionsFontSelectionMenuItem, OStrings.getString("TF_MENU_DISPLAY_FONT"));
        optionsFontSelectionMenuItem.addActionListener(this);

        optionsMenu.add(optionsFontSelectionMenuItem);

        Mnemonics.setLocalizedText(optionsSetupFileFiltersMenuItem, OStrings.getString("TF_MENU_DISPLAY_FILTERS"));
        optionsSetupFileFiltersMenuItem.addActionListener(this);

        optionsMenu.add(optionsSetupFileFiltersMenuItem);

        Mnemonics.setLocalizedText(optionsSentsegMenuItem, OStrings.getString("MW_OPTIONSMENU_SENTSEG"));
        optionsSentsegMenuItem.addActionListener(this);

        optionsMenu.add(optionsSentsegMenuItem);

        Mnemonics.setLocalizedText(optionsSpellCheckMenuItem, OStrings.getString("MW_OPTIONSMENU_SPELLCHECK"));
        optionsSpellCheckMenuItem.addActionListener(this);

        optionsMenu.add(optionsSpellCheckMenuItem);

        Mnemonics.setLocalizedText(optionsWorkflowMenuItem, OStrings.getString("MW_OPTIONSMENU_WORKFLOW"));
        optionsWorkflowMenuItem.addActionListener(this);

        optionsMenu.add(optionsWorkflowMenuItem);

        Mnemonics.setLocalizedText(optionsRestoreGUIMenuItem, OStrings.getString("MW_OPTIONSMENU_RESTORE_GUI"));
        optionsRestoreGUIMenuItem.addActionListener(this);

        optionsMenu.add(optionsRestoreGUIMenuItem);

        helpContentsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        Mnemonics.setLocalizedText(helpContentsMenuItem, OStrings.getString("TF_MENU_HELP_CONTENTS"));
        helpContentsMenuItem.addActionListener(this);

        helpMenu.add(helpContentsMenuItem);

        Mnemonics.setLocalizedText(helpAboutMenuItem, OStrings.getString("TF_MENU_HELP_ABOUT"));
        helpAboutMenuItem.addActionListener(this);

        helpMenu.add(helpAboutMenuItem);

        setActionCommands();

        mainMenu.add(projectMenu);
        mainMenu.add(editMenu);
        mainMenu.add(gotoMenu);
        mainMenu.add(viewMenu);
        mainMenu.add(toolsMenu);
        mainMenu.add(optionsMenu);
        mainMenu.add(helpMenu);

        return mainMenu;
    }

    /**
     * Create menu instance and set title.
     * 
     * @param titleKey
     *            title name key in resource bundle
     * @return menu instance
     */
    private JMenu createMenu(final String titleKey) {
        JMenu result = new JMenu();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        return result;
    }

    /**
     * Create menu item instance and set title.
     * 
     * @param titleKey
     *            title name key in resource bundle
     * @return menu item instance
     */
    private JMenuItem createMenuItem(final String titleKey) {
        JMenuItem result = new JMenuItem();
        Mnemonics.setLocalizedText(result, OStrings.getString(titleKey));
        result.addActionListener(this);
        return result;
    }

    /**
     * Set 'actionCommand' for all menu items. TODO: change to key from resource
     * bundle values
     */
    protected void setActionCommands() {
        try {
            for (Field f : this.getClass().getDeclaredFields()) {
                if (JMenuItem.class.isAssignableFrom(f.getType()) && f.getType() != JMenu.class) {
                    JMenuItem menuItem = (JMenuItem) f.get(this);
                    menuItem.setActionCommand(f.getName());
                }
            }
        } catch (IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Sets the shortcut keys. Need to do it here (manually), because on MacOSX
     * the shortcut key is CMD, and on other OSes it's Ctrl.
     */
    void initUIShortcuts() {
        setAccelerator(projectOpenMenuItem, KeyEvent.VK_O);
        setAccelerator(projectSaveMenuItem, KeyEvent.VK_S);
        setAccelerator(projectEditMenuItem, KeyEvent.VK_E);
        setAccelerator(projectExitMenuItem, KeyEvent.VK_Q);

        setAccelerator(editUndoMenuItem, KeyEvent.VK_Z);
        setAccelerator(editRedoMenuItem, KeyEvent.VK_Y);
        setAccelerator(editOverwriteTranslationMenuItem, KeyEvent.VK_R);
        setAccelerator(editInsertTranslationMenuItem, KeyEvent.VK_I);
        setAccelerator(editOverwriteSourceMenuItem, KeyEvent.VK_R, true);
        setAccelerator(editInsertSourceMenuItem, KeyEvent.VK_I, true);
        setAccelerator(editFindInProjectMenuItem, KeyEvent.VK_F);
        setAccelerator(editSelectFuzzy1MenuItem, KeyEvent.VK_1);
        setAccelerator(editSelectFuzzy2MenuItem, KeyEvent.VK_2);
        setAccelerator(editSelectFuzzy3MenuItem, KeyEvent.VK_3);
        setAccelerator(editSelectFuzzy4MenuItem, KeyEvent.VK_4);
        setAccelerator(editSelectFuzzy5MenuItem, KeyEvent.VK_5);

        setAccelerator(gotoNextUntranslatedMenuItem, KeyEvent.VK_U);
        setAccelerator(gotoNextSegmentMenuItem, KeyEvent.VK_N);
        setAccelerator(gotoPreviousSegmentMenuItem, KeyEvent.VK_P);
        setAccelerator(gotoSegmentMenuItem, KeyEvent.VK_J);

        setAccelerator(gotoHistoryForwardMenuItem, KeyEvent.VK_N, true);
        setAccelerator(gotoHistoryBackMenuItem, KeyEvent.VK_P, true);

        setAccelerator(viewFileListMenuItem, KeyEvent.VK_L);

        setAccelerator(toolsValidateTagsMenuItem, KeyEvent.VK_T);
    }

    /**
     * Utility method to set Ctrl + key accelerators for menu items.
     * 
     * @param key
     *            integer specifiyng the key code (e.g. KeyEvent.VK_Z)
     */
    private void setAccelerator(JMenuItem item, int key) {
        setAccelerator(item, key, false);
    }

    /**
     * Utility method to set Ctrl + key accelerators for menu items.
     * 
     * @param key
     *            integer specifiyng the key code (e.g. KeyEvent.VK_Z)
     */
    private void setAccelerator(JMenuItem item, int key, boolean shift) {
        int shiftmask = shift ? KeyEvent.SHIFT_MASK : 0;
        item.setAccelerator(KeyStroke.getKeyStroke(key, shiftmask
                | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Updates menu items (enables/disables) upon <b>opening</b> project
     */
    void uiUpdateOnProjectOpen() {
        projectNewMenuItem.setEnabled(false);
        projectOpenMenuItem.setEnabled(false);

        projectImportMenuItem.setEnabled(true);
        projectWikiImportMenuItem.setEnabled(true);
        projectReloadMenuItem.setEnabled(true);
        projectCloseMenuItem.setEnabled(true);
        projectSaveMenuItem.setEnabled(true);
        projectEditMenuItem.setEnabled(true);
        projectCompileMenuItem.setEnabled(true);

        editMenu.setEnabled(true);
        editFindInProjectMenuItem.setEnabled(true);
        editInsertSourceMenuItem.setEnabled(true);
        editInsertTranslationMenuItem.setEnabled(true);
        editOverwriteSourceMenuItem.setEnabled(true);
        editOverwriteTranslationMenuItem.setEnabled(true);
        editRedoMenuItem.setEnabled(true);
        editSelectFuzzy1MenuItem.setEnabled(true);
        editSelectFuzzy2MenuItem.setEnabled(true);
        editSelectFuzzy3MenuItem.setEnabled(true);
        editSelectFuzzy4MenuItem.setEnabled(true);
        editSelectFuzzy5MenuItem.setEnabled(true);
        editUndoMenuItem.setEnabled(true);

        gotoMenu.setEnabled(true);
        gotoNextSegmentMenuItem.setEnabled(true);
        gotoNextUntranslatedMenuItem.setEnabled(true);
        gotoPreviousSegmentMenuItem.setEnabled(true);
        gotoSegmentMenuItem.setEnabled(true);

        viewFileListMenuItem.setEnabled(true);
        toolsValidateTagsMenuItem.setEnabled(true);

        switchCaseSubMenu.setEnabled(true);
    }

    /**
     * Updates menu items (enables/disables) upon <b>closing</b> project
     */
    void uiUpdateOnProjectClose() {
        projectNewMenuItem.setEnabled(true);
        projectOpenMenuItem.setEnabled(true);

        projectImportMenuItem.setEnabled(false);
        projectWikiImportMenuItem.setEnabled(false);
        projectReloadMenuItem.setEnabled(false);
        projectCloseMenuItem.setEnabled(false);
        projectSaveMenuItem.setEnabled(false);
        projectEditMenuItem.setEnabled(false);
        projectCompileMenuItem.setEnabled(false);

        editMenu.setEnabled(false);
        editFindInProjectMenuItem.setEnabled(false);
        editInsertSourceMenuItem.setEnabled(false);
        editInsertTranslationMenuItem.setEnabled(false);
        editOverwriteSourceMenuItem.setEnabled(false);
        editOverwriteTranslationMenuItem.setEnabled(false);
        editRedoMenuItem.setEnabled(false);
        editSelectFuzzy1MenuItem.setEnabled(false);
        editSelectFuzzy2MenuItem.setEnabled(false);
        editSelectFuzzy3MenuItem.setEnabled(false);
        editSelectFuzzy4MenuItem.setEnabled(false);
        editSelectFuzzy5MenuItem.setEnabled(false);
        editUndoMenuItem.setEnabled(false);

        gotoMenu.setEnabled(false);
        gotoNextSegmentMenuItem.setEnabled(false);
        gotoNextUntranslatedMenuItem.setEnabled(false);
        gotoPreviousSegmentMenuItem.setEnabled(false);
        gotoSegmentMenuItem.setEnabled(false);

        viewFileListMenuItem.setEnabled(false);
        toolsValidateTagsMenuItem.setEnabled(false);

        switchCaseSubMenu.setEnabled(false);
    }

    JMenuItem cycleSwitchCaseMenuItem;
    JMenuItem editFindInProjectMenuItem;
    JMenuItem editInsertSourceMenuItem;
    JMenuItem editInsertTranslationMenuItem;
    JMenu editMenu;
    JMenuItem editOverwriteSourceMenuItem;
    JMenuItem editOverwriteTranslationMenuItem;
    JMenuItem editRedoMenuItem;
    JMenuItem editSelectFuzzy1MenuItem;
    JMenuItem editSelectFuzzy2MenuItem;
    JMenuItem editSelectFuzzy3MenuItem;
    JMenuItem editSelectFuzzy4MenuItem;
    JMenuItem editSelectFuzzy5MenuItem;
    JMenuItem editUndoMenuItem;
    JMenuItem gotoHistoryBackMenuItem;
    JMenuItem gotoHistoryForwardMenuItem;
    JMenu gotoMenu;
    JMenuItem gotoNextSegmentMenuItem;
    JMenuItem gotoNextUntranslatedMenuItem;
    JMenuItem gotoPreviousSegmentMenuItem;
    JMenuItem gotoSegmentMenuItem;
    JMenuItem helpAboutMenuItem;
    JMenuItem helpContentsMenuItem;
    JMenu helpMenu;
    JMenuItem lowerCaseMenuItem;
    JMenuBar mainMenu;
    JCheckBoxMenuItem optionsAlwaysConfirmQuitCheckBoxMenuItem;
    JMenuItem optionsFontSelectionMenuItem;
    JMenu optionsMenu;
    JMenuItem optionsRestoreGUIMenuItem;
    JMenuItem optionsSentsegMenuItem;
    JMenuItem optionsSetupFileFiltersMenuItem;
    JMenuItem optionsSpellCheckMenuItem;
    JCheckBoxMenuItem optionsTabAdvanceCheckBoxMenuItem;
    JMenuItem optionsWorkflowMenuItem;
    JMenuItem projectCloseMenuItem;
    JMenuItem projectCompileMenuItem;
    JMenuItem projectEditMenuItem;
    JMenuItem projectExitMenuItem;
    JMenuItem projectImportMenuItem;
    JMenu projectMenu;
    JMenuItem projectNewMenuItem;
    JMenuItem projectOpenMenuItem;
    JMenuItem projectReloadMenuItem;
    JMenuItem projectSaveMenuItem;
    JMenuItem projectWikiImportMenuItem;
    JMenu switchCaseSubMenu;
    JMenuItem titleCaseMenuItem;
    JMenu toolsMenu;
    JMenuItem toolsValidateTagsMenuItem;
    JMenuItem upperCaseMenuItem;
    JCheckBoxMenuItem viewDisplaySegmentSourceCheckBoxMenuItem;
    JMenuItem viewFileListMenuItem;
    JCheckBoxMenuItem viewMarkTranslatedSegmentsCheckBoxMenuItem;
    JCheckBoxMenuItem viewMarkUntranslatedSegmentsCheckBoxMenuItem;
    JMenu viewMenu;
}
