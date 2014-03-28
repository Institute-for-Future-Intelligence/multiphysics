package com.apple.eawt;

public interface ApplicationListener {

	void handleAbout(ApplicationEvent event);

	void handleOpenApplication(ApplicationEvent event);

	void handleOpenFile(ApplicationEvent event);

	void handlePreferences(ApplicationEvent event);

	void handlePrintFile(ApplicationEvent event);

	void handleQuit(ApplicationEvent event);

}
