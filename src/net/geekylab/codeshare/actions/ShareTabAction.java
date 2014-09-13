package net.geekylab.codeshare.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import net.geekylab.codeshare.actions.dialog.ShareTabDialog;
import net.geekylab.codeshare.network.PeerDiscovery;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by johna on 08/09/14.
 */
public class ShareTabAction extends AnAction {
    private Project project;
    private ShareTabDialog dialog;
    private VirtualFile virtualFile;

    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        final ShareTabDialog dialog = getDialog();
        dialog.show();
        if (dialog.isOK()) {
            final PeerDiscovery.Peer selectedItem = (PeerDiscovery.Peer) dialog.peerListModel.getSelectedItem();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    virtualFile.getFileSystem().addVirtualFileListener(new VirtualFileAdapter() {
                        @Override
                        public void contentsChanged(@NotNull VirtualFileEvent event) {
                            try {
                                VirtualFile file = event.getFile();
                                if (file.equals(virtualFile)) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("filename", file.getName());
                                    Socket socket = new Socket(selectedItem.ip, 12033);
                                    InputStream in = file.getInputStream();
                                    OutputStream out = socket.getOutputStream();

                                    byte[] buffer = new byte[(int) file.getLength()];
                                    int read = in.read(buffer, 0, buffer.length);
                                    String msg = new String(buffer, 0, read);
                                    jsonObject.put("contents", msg);

                                    byte[] jsonBytes = jsonObject.toString().getBytes();
                                    out.write(jsonBytes, 0, jsonBytes.length);
                                    out.flush();
                                    socket.close();
                                    in.close();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            }).start();


        }


    }


    public ShareTabDialog getDialog() {
        return new ShareTabDialog(project, virtualFile);
    }
}
