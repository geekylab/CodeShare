package net.geekylab.codeshare.actions.dialog;

import com.intellij.ide.util.gotoByName.SimpleChooseByNameModel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by johna on 09/09/14.
 */
public class DiscoveryPeerPopupModel extends SimpleChooseByNameModel {
    protected DiscoveryPeerPopupModel(@NotNull Project project) {
        super(project, "Enter peer name:", null);
    }

    @Override
    public String[] getNames() {
        return new String[0];
    }

    @Override
    protected Object[] getElementsByName(String s, String s2) {
        return new Object[0];
    }

    @Override
    public ListCellRenderer getListCellRenderer() {

        return new MyCellRenderer();
    }

    @Nullable
    @Override
    public String getElementName(Object o) {
        return null;
    }

    private class MyCellRenderer extends JLabel implements ListCellRenderer {
        public MyCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            setText(value.toString());
            setBackground(isSelected ? JBColor.RED : JBColor.white);
            setForeground(isSelected ? JBColor.white : JBColor.black);

            return this;
        }
    }
}
