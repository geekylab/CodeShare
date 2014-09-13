package net.geekylab.codeshare;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import net.geekylab.codeshare.network.PeerDiscoverResponseInterface;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by johna on 12/09/14.
 */
public class TabManager implements PeerDiscoverResponseInterface {
    private static TabManager INSTANCE;
    private Project project;
    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
    private String shareCodeFolderPath = "/tmp/sharecode/";

    public TabManager(Project project) {
        this.project = project;
        shareCodeFolderPath = PathManager.getPluginsPath() + "/sharecode/";
    }

    public VirtualFile openNewTab(String fileName, String content) {
        if (createFile(fileName, content)) {
            VirtualFile file = virtualFileBy(fileName);
            if (file != null)
                new OpenFileDescriptor(project, file).navigate(true);

            return file;
        }
        return null;
    }

    public boolean createFile(final String fileName, final String text) {
        return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                try {
                    ensureExists(new File(shareCodeFolderPath));
                    VirtualFile scratchesFolder = virtualFileBy("");
                    if (scratchesFolder == null) return false;
                    VirtualFile codeFile = scratchesFolder.createChildData(null, fileName);
                    codeFile.setBinaryContent(text.getBytes("UTF-8"));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    @Nullable
    public VirtualFile virtualFileBy(String fileName) {
        return fileManager.refreshAndFindFileByUrl("file://" + shareCodeFolderPath + fileName);
    }

    private static void ensureExists(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException(CommonBundle.message("exception.directory.can.not.create", dir.getPath()));
        }
    }


    @Override
    public void response(final String message) {
        EventQueue.invokeLater(new Runnable() {
            //@Override
            public void run() {
                JSONObject jsonObject = new JSONObject(message);

                String filename = jsonObject.getString("filename");
                String contents = jsonObject.getString("contents");

                VirtualFile virtualFile = openNewTab(filename, contents);
            }
        });
    }
}
