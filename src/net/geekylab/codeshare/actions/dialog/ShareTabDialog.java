package net.geekylab.codeshare.actions.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import net.geekylab.codeshare.CodeShareComponent;
import net.geekylab.codeshare.network.PeerDiscovery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by johna on 08/09/14.
 */
public class ShareTabDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile virtualFile;
    protected JBLabel label;
    private JPanel contentPane;
    private ComboBox peerList;
    public DefaultComboBoxModel<PeerDiscovery.Peer> peerListModel;

    public ShareTabDialog(@NotNull final Project project, VirtualFile vfile) {
        super(project);
        this.virtualFile = vfile;
        this.project = project;
        setTitle("Share Tab");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PeerDiscovery peerDiscovery = CodeShareComponent.getInstance(project).getPeerDiscovery();
                    final PeerDiscovery.Peer[] peers = peerDiscovery.getPeers(100, (byte) 0);
                    System.out.println(peers.length + " peers found");

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            StringBuilder b = new StringBuilder();
                            for (PeerDiscovery.Peer p : peers) {
                                peerListModel.addElement(p);
                            }
                            label.setText(b.toString());


                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        init();
    }

    @Override
    protected void doOKAction() {

//        PeerDiscovery.Peer selectedPeer = (PeerDiscovery.Peer) peerListModel.getSelectedItem();
//        PeerDiscovery peerDiscovery = ShareCodeComponent.getInstance(project).getPeerDiscovery();
//        try {
//            peerDiscovery.sendShareTab(obj, virtualFile.getName(), virtualFile.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        super.doOKAction();

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();

        label = new JBLabel();
        label.setText("loading....");
        contentPane.add(label);

        peerListModel = new DefaultComboBoxModel<PeerDiscovery.Peer>();
        peerList = new ComboBox(peerListModel);
        contentPane.add(peerList);
        return contentPane;
    }
}
