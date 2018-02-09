import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Klasa nadpisująca zachowanie klasy bazowej TransferHandler, tak by można było zaimplementować zamianę tekstu pomiędzy dwoma JTextFieldami
 * @see <a href="https://stackoverflow.com/questions/7976972/swing-jtextfield-dnd-replace-the-existing-text-with-the-imported-text">https://stackoverflow.com/questions/7976972/swing-jtextfield-dnd-replace-the-existing-text-with-the-imported-text</a>
 */
public class CustomTransferHandler extends TransferHandler {
    static String toReplace = "";
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public Transferable createTransferable(JComponent c) {
        return new StringSelection(((JTextComponent) c).getText());
    }

    public void exportDone(JComponent c, Transferable t, int action) {
        if(action == MOVE)
            ((JTextComponent) c).setText(toReplace);
    }

    public boolean canImport(TransferSupport ts) {
        return ts.getComponent() instanceof JTextComponent;
    }

    public boolean importData(TransferSupport ts) {
        try {
            toReplace = ((JTextField) ts.getComponent()).getText();
            ((JTextComponent) ts.getComponent())
                    .setText((String) ts
                            .getTransferable()
                            .getTransferData(DataFlavor.stringFlavor));
            return true;
        } catch(UnsupportedFlavorException e) {
            return false;
        } catch(IOException e) {
            return false;
        }
    }
}
