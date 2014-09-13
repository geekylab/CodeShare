package net.geekylab.codeshare;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import net.geekylab.codeshare.network.PeerDiscovery;
import net.geekylab.codeshare.network.tcp.ShareCodeTcpServer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by johna on 12/09/14.
 */
public class CodeShareComponent implements ProjectComponent {

    private Project project;
    private TabManager tabManager;
    private PeerDiscovery peerDiscovery;
    private ShareCodeTcpServer shareCodeTcpServer;

    public CodeShareComponent(Project project) {
        this.project = project;
    }

    public static CodeShareComponent getInstance(Project project) {
        return project.getComponent(CodeShareComponent.class);
    }


    public void initComponent() {
        tabManager = new TabManager(project);
        peerDiscovery = PeerDiscovery.getInstance(tabManager);
        shareCodeTcpServer = new ShareCodeTcpServer();
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "ShareCodeComponent";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public PeerDiscovery getPeerDiscovery() {
        return peerDiscovery;
    }

    public ShareCodeTcpServer getShareCodeTcpServer() {
        return shareCodeTcpServer;
    }
}
