/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.semux.gui.Action;
import org.semux.gui.AddressBook;
import org.semux.gui.AddressBook.Entry;
import org.semux.gui.SwingUtil;
import org.semux.gui.model.WalletModel;
import org.semux.message.GuiMessages;
import org.semux.util.exception.UnreachableException;

public class AddressBookDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String[] columnNames = { GuiMessages.get("Name"), GuiMessages.get("Address") };

    private final transient WalletModel model;

    private final JTable table;
    private final AddressTableModel tableModel;

    public AddressBookDialog(JFrame parent, WalletModel model) {
        super(null, GuiMessages.get("AddressBook"), Dialog.ModalityType.MODELESS);
        setName("AddressBookDialog");
        this.model = model;

        tableModel = new AddressTableModel();
        table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(25);
        table.getTableHeader().setPreferredSize(new Dimension(10000, 24));
        SwingUtil.setColumnWidths(table, 800, 0.25, 0.75);
        SwingUtil.setColumnAlignments(table, false, false);

        // auto sort
        table.setAutoCreateRowSorter(true);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);
        JButton btnNew = SwingUtil.createDefaultButton(GuiMessages.get("Add"), this, Action.ADD_ADDRESS);
        panel.add(btnNew);
        JButton btnCopy = SwingUtil.createDefaultButton(GuiMessages.get("Copy"), this, Action.COPY_ADDRESS);
        panel.add(btnCopy);
        JButton btnDelete = SwingUtil.createDefaultButton(GuiMessages.get("Delete"), this, Action.DELETE_ADDRESS);
        panel.add(btnDelete);

        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(table);

        this.setTitle(GuiMessages.get("AddressBook"));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setIconImage(SwingUtil.loadImage("logo", 128, 128).getImage());
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
        this.setModal(false);

        // display data
        refresh();
    }

    class AddressTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private transient List<Entry> addresses;

        AddressTableModel() {
            this.addresses = Collections.emptyList();
        }

        void setData(List<Entry> addresses) {
            this.addresses = addresses;
            this.fireTableDataChanged();
        }

        Entry getRow(int row) {
            if ((row >= 0) && (row < addresses.size())) {
                return addresses.get(row);
            }

            return null;
        }

        @Override
        public int getRowCount() {
            return addresses.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Entry entry = addresses.get(row);

            switch (column) {
            case 0:
                return entry.getName();
            case 1:
                return entry.getAddress();
            default:
                return null;
            }
        }
    }

    private Entry getSelectedEntry() {
        int row = table.getSelectedRow();
        return (row != -1) ? tableModel.getRow(table.convertRowIndexToModel(row)) : null;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        Action action = Action.valueOf(e.getActionCommand());

        switch (action) {
        case REFRESH:
            refresh();
            break;
        case ADD_ADDRESS:
            AddAddressDialog dialog = new AddAddressDialog(this);
            dialog.setVisible(true);
            break;
        case COPY_ADDRESS:
        case DELETE_ADDRESS:
            Entry entry = getSelectedEntry();
            if (entry != null) {
                if (action == Action.COPY_ADDRESS) {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    cb.setContents(new StringSelection(entry.getAddress()), null);

                    JOptionPane.showMessageDialog(this, GuiMessages.get("AddressCopied", entry.getAddress()));
                } else {
                    getAddressBook().remove(entry.getName());
                    refresh();
                }
            } else {
                JOptionPane.showMessageDialog(this, GuiMessages.get("SelectAddress"));
            }
            break;
        default:
            throw new UnreachableException();
        }
    }

    protected AddressBook getAddressBook() {
        return model.getAddressBook();
    }

    protected void refresh() {
        List<Entry> list = getAddressBook().list();

        /*
         * update table model
         */
        Entry e = getSelectedEntry();
        tableModel.setData(list);

        if (e != null) {
            for (int i = 0; i < list.size(); i++) {
                if (e.getName().equals(list.get(i).getName())) {
                    table.setRowSelectionInterval(table.convertRowIndexToView(i), table.convertRowIndexToView(i));
                    break;
                }
            }
        }
    }
}