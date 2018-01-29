/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui.panel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.Optional;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Timeout;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semux.KernelMock;
import org.semux.core.state.Account;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.gui.model.WalletAccount;
import org.semux.gui.model.WalletModel;
import org.semux.message.GuiMessages;
import org.semux.rules.KernelRule;

@RunWith(MockitoJUnitRunner.class)
public class ReceivePanelTest extends AssertJSwingJUnitTestCase {

    @Rule
    public KernelRule kernelRule1 = new KernelRule(51610, 51710);

    @Mock
    WalletModel walletModel;

    ReceivePanelTestApplication application;

    FrameFixture window;

    @Override
    protected void onSetUp() {

    }

    @Test
    public void testCopyAddress() {
        Key key1 = new Key();
        Key key2 = new Key();
        WalletAccount acc1 = new WalletAccount(key1, new Account(key1.toAddress(), 1, 1, 1), Optional.empty());
        WalletAccount acc2 = new WalletAccount(key2, new Account(key2.toAddress(), 2, 2, 2), Optional.empty());

        // mock walletModel
        when(walletModel.getAccounts()).thenReturn(Arrays.asList(acc1, acc2));

        // mock kernel
        KernelMock kernelMock = spy(kernelRule1.getKernel());
        application = GuiActionRunner.execute(() -> new ReceivePanelTestApplication(walletModel, kernelMock));

        window = new FrameFixture(robot(), application);
        window.show().requireVisible().moveToFront();

        JTableFixture table = window.table("accountsTable").requireVisible().requireRowCount(2);
        table.cell(Hex.PREF + key2.toAddressString()).click();
        table.requireSelectedRows(1);
        window.button("btnCopyAddress").requireVisible().click();
        window.optionPane(Timeout.timeout(1000)).requireVisible()
                .requireMessage(GuiMessages.get("AddressCopied", Hex.PREF + key2.toAddressString()));

        assertEquals(Hex.PREF + key2.toAddressString(), GuiActionRunner
                .execute(() -> Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)));
    }

    @Test
    public void testRenameAddress() throws InterruptedException {
        Key key1 = new Key();
        Key key2 = new Key();
        WalletAccount acc1 = new WalletAccount(key1, new Account(key1.toAddress(), 1, 1, 1), Optional.empty());
        WalletAccount acc2 = new WalletAccount(key2, new Account(key2.toAddress(), 2, 2, 2), Optional.empty());

        // mock walletModel
        when(walletModel.getAccounts()).thenReturn(Arrays.asList(acc1, acc2));

        // mock kernel
        KernelMock kernelMock = spy(kernelRule1.getKernel());
        application = GuiActionRunner.execute(() -> new ReceivePanelTestApplication(walletModel, kernelMock));

        window = new FrameFixture(robot(), application);
        window.show().requireVisible().moveToFront();

        JTableFixture table = window.table("accountsTable").requireVisible().requireRowCount(2);
        table.cell(Hex.PREF + key2.toAddressString()).click();
        table.requireSelectedRows(1);
        window.button("btnRenameAddress").requireVisible().click();

        JOptionPaneFixture optionPane = window.optionPane(Timeout.timeout(1000)).requireVisible();
        optionPane.textBox().enterText("c");

        optionPane.buttonWithText("OK").click();

        assertEquals("c", kernelMock.getWallet().getAddressAlias(key2.toAddress()).get());
    }
}
