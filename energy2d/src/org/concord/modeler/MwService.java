package org.concord.modeler;

import java.awt.Component;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import javax.swing.JPopupMenu;

public interface MwService {

	public void setEditable(boolean b);

	public Component getSnapshotComponent();

	public JPopupMenu getPopupMenu();

	public String runNativeScript(String script);

	public void loadState(InputStream is) throws IOException;

	public void saveState(OutputStream os) throws IOException;

	public boolean needExecutorService();

	public void setExecutorService(ExecutorService service);

}