package net.geekylab.codeshare.actions;

import com.google.gson.JsonParseException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import net.geekylab.codeshare.CodeShareComponent;
import net.geekylab.codeshare.network.PeerDiscovery;
import net.geekylab.codeshare.Icons;
import net.geekylab.codeshare.network.ReceivedInterface;
import net.geekylab.codeshare.network.tcp.ShareCodeTcpServer;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

/**
 * Created by johna on 08/09/14.
 * by Geekylab
 */
public class ShareServerAction extends AnAction {


    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null)
            return;

        ShareCodeTcpServer tcpServer = CodeShareComponent.getInstance(project).getShareCodeTcpServer();

        tcpServer.setReceivedListener(new ReceivedInterface() {
            @Override
            public void onReceived(final String message) {
                EventQueue.invokeLater(new Runnable() {
                    //@Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            String filename = jsonObject.getString("filename");
                            String contents = jsonObject.getString("contents");
                            CodeShareComponent.getInstance(project).getTabManager().openNewTab(filename, contents);
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        if (!tcpServer.isRunning()) {
            try {
                tcpServer.start();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                tcpServer.stop();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


        PeerDiscovery peerDiscovery = CodeShareComponent.getInstance(project).getPeerDiscovery();
        if (peerDiscovery.isRunning()) {
            peerDiscovery.disconnect();
        } else {
            peerDiscovery.connect();
        }
    }

    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null)
            return;

        PeerDiscovery peerDiscovery = CodeShareComponent.getInstance(project).getPeerDiscovery();
        if (peerDiscovery.isRunning()) {
            e.getPresentation().setIcon(Icons.ICONSTOP16);
        } else {
            e.getPresentation().setIcon(Icons.ICONSTART16);
        }

        super.update(e);
    }

}
