package org.energy2d.system;

import java.awt.Desktop;
import java.awt.Frame;
import java.net.URI;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 */
public class Helper {

    private static ScriptDialog scriptDialog;

    private Helper() {
    }

    static void showScriptDialog(System2D box) {
        if (scriptDialog != null && scriptDialog.isShowing()) {
            scriptDialog.toFront();
        } else {
            scriptDialog = new ScriptDialog(box);
            scriptDialog.pack();
            scriptDialog.setLocationRelativeTo(box.view);
            scriptDialog.setVisible(true);
        }
    }

    static void openBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String os = System.getProperty("os.name");
            try {
                if (os.startsWith("Windows")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.startsWith("Mac OS")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showKeyboardShortcuts(Frame frame) {
        String s = "<html><h2>Keyboard Shortcuts</h2><hr>";
        s += "<br><font face=Courier>'R'</font> &mdash; Run or pause the simulation.";
        s += "<br><font face=Courier>'T'</font> &mdash; Reset the simulation.";
        s += "<br><font face=Courier>'L'</font> &mdash; Reload the initial configurations.";
        s += "<br><font face=Courier>'G'</font> &mdash; Show or hide the graph.";
        s += "<br><font face=Courier>'DELETE'</font> &mdash; Erase data in the graph when it is being shown.";
        s += "<br><font face=Courier>'S'</font> &mdash; Turn sunlight on or off.";
        s += "<br><font face=Courier>'Q'</font> &mdash; When sunlight is present, increase the sun angle (towards west).";
        s += "<br><font face=Courier>'W'</font> &mdash; When sunlight is present, decrease the sun angle (towards east).";
        s += "</html>";
        JOptionPane.showMessageDialog(frame, new JLabel(s), "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showAbout(Frame frame) {
        String s = "<html><h3>" + System2D.BRAND_NAME + "</h3>";
        s += "<h4><i>Interactive Multiphysics Simulations for Everyone</i></h4>";
        s += "https://energy2d.org<br>Energy2D, &copy; 2021-" + Calendar.getInstance().get(Calendar.YEAR);
        s += "<hr>";
        s += "<h4>Credit:</h4>This program was created by Dr. Charles Xie. Funding was provided by the National Science Foundation<br>under grants 0918449, 1124281, 1304485, and 1512868 that were awarded to Dr. Xie.";
        s += "<h4>License:</h4>This software is provided to you as it is under the MIT License.";
        s += "</html>";
        JOptionPane.showMessageDialog(frame, new JLabel(s), "About " + System2D.BRAND_NAME, JOptionPane.INFORMATION_MESSAGE, System2D.isApplet() ? null : new ImageIcon(System2D.class.getResource("resources/frame.png")));
    }

}