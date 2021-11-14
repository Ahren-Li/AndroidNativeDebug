package org.ahren.android.ui;

import com.jetbrains.cidr.cpp.execution.remote.DebuggerSourceKind;
import com.jetbrains.cidr.ui.BaseListRenderer;
import com.jetbrains.cidr.ui.CustomEditableComboItem;
import com.jetbrains.cidr.ui.FileItemPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class MyDebuggersRenderer extends BaseListRenderer {

    private final boolean isRemote;

    @Nullable
    protected String getSeparatorAbove(@NotNull JList list, @Nullable Object value, int index) {

        if (index == 0) {
            return "";
        }
        Object previous = list.getModel().getElementAt(index - 1);
        if (value instanceof DebuggerSourceKind.Toolchain && !(previous instanceof DebuggerSourceKind.Toolchain)) {
            return "";
        } else if (value instanceof File && !(previous instanceof File)) {
            return "";
        } else {
            return value instanceof CustomEditableComboItem ? "" : super.getSeparatorAbove(list, value, index);
        }

    }

    public final boolean isRemote() {
        return this.isRemote;
    }

    public MyDebuggersRenderer(boolean isRemote) {
        super(new FileItemPrinter(true), DebuggerSourceKind.Bundle.Companion.getPrinter(), DebuggerSourceKind.Toolchain.Companion.printer(isRemote), DebuggerSourceKind.External.Companion.getPrinter());
        this.isRemote = isRemote;
    }

}
