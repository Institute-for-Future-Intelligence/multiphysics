package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.math.Blob2D;
import org.energy2d.math.EllipticalAnnulus;
import org.energy2d.math.Polygon2D;
import org.energy2d.math.Annulus;
import org.energy2d.math.TransformableShape;
import org.energy2d.model.Part;
import org.energy2d.undo.UndoFlipManipulable;
import org.energy2d.undo.UndoResizeManipulable;
import org.energy2d.undo.UndoRotateManipulable;
import org.energy2d.undo.UndoScaleManipulable;
import org.energy2d.undo.UndoShearManipulable;
import org.energy2d.undo.UndoTranslateManipulable;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class PartModelDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.########");

	private JTextField thermalConductivityField;
	private JTextField specificHeatField;
	private JTextField densityField;
	private JLabel powerLabel;
	private JTextField powerField;
	private JTextField temperatureCoefficientField;
	private JTextField referenceTemperatureField;
	private JLabel temperatureLabel;
	private JTextField temperatureField;
	private JTextField windSpeedField;
	private JTextField windAngleField;
	private JRadioButton noScatteringRadioButton;
	private JRadioButton visibleScatteringRadioButton;
	private JRadioButton invisibleScatteringRadioButton;
	private JTextField emissivityField, absorptivityField, reflectivityField, transmissivityField;
	private JTextField elasticityField;
	private JTextField xField, yField, wField, hField, angleField, scaleXField, scaleYField, shearXField, shearYField;
	private JTextField innerDiameterField, outerDiameterField;
	private JTextField innerAField, innerBField, outerAField, outerBField;
	private JCheckBox flipXCheckBox, flipYCheckBox;
	private JTextField uidField;
	private JTextField labelField;
	private JRadioButton notHeatSourceRadioButton;
	private JRadioButton powerRadioButton;
	private JRadioButton constantTemperatureRadioButton;
	private Window owner;
	private ActionListener okListener;

	PartModelDialog(final View2D view, final Part part, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Part (#" + view.model.getParts().indexOf(part) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (powerRadioButton.isSelected()) {
					if (powerField.getText().equals("0")) {
						powerField.selectAll();
						JOptionPane.showMessageDialog(owner, "Did you forget to set the power of the source?", "Reminder", JOptionPane.INFORMATION_MESSAGE);
						powerField.requestFocusInWindow();
						return;
					}
				}

				// currently, a photon is either absorbed, reflected, or transmitted. Scattering is a special case of reflection.
				boolean visibleScattering = visibleScatteringRadioButton.isSelected();
				boolean invisibleScattering = invisibleScatteringRadioButton.isSelected();
				boolean scattering = visibleScattering || invisibleScattering;

				float absorptivity = parse(absorptivityField.getText());
				if (Float.isNaN(absorptivity))
					return;
				float reflectivity = parse(reflectivityField.getText());
				if (Float.isNaN(reflectivity))
					return;
				float transmissivity = parse(transmissivityField.getText());
				if (Float.isNaN(transmissivity))
					return;
				float emissivity = parse(emissivityField.getText());
				if (Float.isNaN(emissivity))
					return;
				float elasticity = parse(elasticityField.getText());
				if (Float.isNaN(elasticity))
					return;

				if (absorptivity < 0 || absorptivity > 1) {
					JOptionPane.showMessageDialog(owner, "Absorptivity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (reflectivity < 0 || reflectivity > 1) {
					JOptionPane.showMessageDialog(owner, "Reflectivity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (transmissivity < 0 || transmissivity > 1) {
					JOptionPane.showMessageDialog(owner, "Transmissivity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (emissivity < 0 || emissivity > 1) {
					JOptionPane.showMessageDialog(owner, "Emissivity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float sum = absorptivity + reflectivity + transmissivity;
				if (Math.abs(sum - 1) > 0.01) {
					JOptionPane.showMessageDialog(owner, "The sum of absorptivity, reflectivity, and transmissivity must be exactly one.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (elasticity < 0 || elasticity > 1) {
					JOptionPane.showMessageDialog(owner, "Elasticity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				float conductivity = parse(thermalConductivityField.getText());
				if (Float.isNaN(conductivity))
					return;
				float specificHeat = parse(specificHeatField.getText());
				if (Float.isNaN(specificHeat))
					return;
				if (specificHeat <= 0) {
					JOptionPane.showMessageDialog(owner, "Specific heat must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float density = parse(densityField.getText());
				if (Float.isNaN(density))
					return;
				if (density <= 0) {
					JOptionPane.showMessageDialog(owner, "Density must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float windSpeed = parse(windSpeedField.getText());
				if (Float.isNaN(windSpeed))
					return;
				float windAngle = parse(windAngleField.getText());
				if (Float.isNaN(windAngle))
					return;
				float xcenter = parse(xField.getText());
				if (Float.isNaN(xcenter))
					return;
				float ycenter = parse(yField.getText());
				if (Float.isNaN(ycenter))
					return;
				float width = Float.NaN;
				if (wField != null) {
					width = parse(wField.getText());
					if (Float.isNaN(width))
						return;
				}
				float height = Float.NaN;
				if (hField != null) {
					height = parse(hField.getText());
					if (Float.isNaN(height))
						return;
				}
				float innerDiameter = Float.NaN;
				if (innerDiameterField != null) {
					innerDiameter = parse(innerDiameterField.getText());
					if (Float.isNaN(innerDiameter))
						return;
				}
				float outerDiameter = Float.NaN;
				if (outerDiameterField != null) {
					outerDiameter = parse(outerDiameterField.getText());
					if (Float.isNaN(outerDiameter))
						return;
				}
				if (innerDiameter >= outerDiameter) {
					JOptionPane.showMessageDialog(owner, "Inner circle must be contained within outer circle.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float innerA = Float.NaN;
				if (innerAField != null) {
					innerA = parse(innerAField.getText());
					if (Float.isNaN(innerA))
						return;
				}
				float innerB = Float.NaN;
				if (innerBField != null) {
					innerB = parse(innerBField.getText());
					if (Float.isNaN(innerB))
						return;
				}
				float outerA = Float.NaN;
				if (outerAField != null) {
					outerA = parse(outerAField.getText());
					if (Float.isNaN(outerA))
						return;
				}
				float outerB = Float.NaN;
				if (outerBField != null) {
					outerB = parse(outerBField.getText());
					if (Float.isNaN(outerB))
						return;
				}
				if (innerA >= outerA || innerB >= outerB) {
					JOptionPane.showMessageDialog(owner, "Inner ellipse must be contained within outer ellipse.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float degree = Float.NaN;
				if (angleField != null) {
					degree = parse(angleField.getText());
					if (Float.isNaN(degree))
						return;
				}
				float scaleX = Float.NaN;
				if (scaleXField != null) {
					scaleX = parse(scaleXField.getText());
					if (Float.isNaN(scaleX))
						return;
					if (scaleX <= 0) {
						JOptionPane.showMessageDialog(owner, "Scale X must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				float scaleY = Float.NaN;
				if (scaleYField != null) {
					scaleY = parse(scaleYField.getText());
					if (Float.isNaN(scaleY))
						return;
					if (scaleY <= 0) {
						JOptionPane.showMessageDialog(owner, "Scale Y must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				float shearX = Float.NaN;
				if (shearXField != null) {
					shearX = parse(shearXField.getText());
					if (Float.isNaN(shearX))
						return;
				}
				float shearY = Float.NaN;
				if (shearYField != null) {
					shearY = parse(shearYField.getText());
					if (Float.isNaN(shearY))
						return;
				}
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(part.getUid())) {
						if (view.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				float temperature = parse(temperatureField.getText());
				if (Float.isNaN(temperature))
					return;
				part.setTemperature(temperature);
				float power = parse(powerField.getText());
				if (notHeatSourceRadioButton.isSelected() || constantTemperatureRadioButton.isSelected()) {
					part.setPower(0);
				} else if (powerRadioButton.isSelected()) {
					if (Float.isNaN(power))
						return;
					part.setPower(power);
				}
				float temperatureCoefficient = parse(temperatureCoefficientField.getText());
				if (Float.isNaN(temperatureCoefficient))
					return;
				part.setThermistorTemperatureCoefficient(temperatureCoefficient);
				float referenceTemperature = parse(referenceTemperatureField.getText());
				if (Float.isNaN(referenceTemperature))
					return;
				part.setThermistorReferenceTemperature(referenceTemperature);
				part.setConstantTemperature(constantTemperatureRadioButton.isSelected());

				Shape shape = part.getShape();
				if (shape instanceof RectangularShape) {
					if (!Float.isNaN(width) && !Float.isNaN(height)) {
						Point2D.Float oldCenter = part.getCenter();
						float x = xcenter - 0.5f * width;
						float y = view.model.getLy() - ycenter - 0.5f * height;
						float dx = 0.000001f * view.model.getLx();
						float dy = 0.000001f * view.model.getLy();
						boolean moved = Math.abs(x - oldCenter.x) > dx || Math.abs(y - oldCenter.y) > dy;
						boolean resized = Math.abs(width - shape.getBounds2D().getWidth()) > dx || Math.abs(height - shape.getBounds2D().getHeight()) > dy;
						if (resized) {
							view.getUndoManager().addEdit(new UndoResizeManipulable(view));
						} else {
							if (moved)
								view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
						}
						if (moved || resized)
							view.resizeManipulableTo(part, x, y, width, height, 0, 0);
					}
				} else if (shape instanceof TransformableShape) {
					TransformableShape s = (TransformableShape) shape;
					if (!Float.isNaN(xcenter) && !Float.isNaN(ycenter)) {
						Point2D.Float oldCenter = s.getCenter();
						float dx = 0.000001f * view.model.getLx();
						float dy = 0.000001f * view.model.getLy();
						boolean moved = Math.abs(xcenter - oldCenter.x) > dx || Math.abs(ycenter - oldCenter.y) > dy;
						if (moved) {
							view.getUndoManager().addEdit(new UndoTranslateManipulable(view));
							s.flipY();
							s.translateBy(xcenter - s.getCenter().x, ycenter - s.getCenter().y);
							float ly = view.model.getLy();
							if (s instanceof Polygon2D) {
								Polygon2D p = (Polygon2D) s;
								int n = p.getVertexCount();
								Point2D.Float v;
								for (int i = 0; i < n; i++) {
									v = p.getVertex(i);
									v.y = ly - v.y;
								}
							} else if (s instanceof Blob2D) {
								Blob2D b = (Blob2D) s;
								int n = b.getPointCount();
								Point2D.Float v;
								for (int i = 0; i < n; i++) {
									v = b.getPoint(i);
									v.y = ly - v.y;
								}
							}
						}
					}
					if (!Float.isNaN(degree) && degree != 0) {
						view.getUndoManager().addEdit(new UndoRotateManipulable(view, degree));
						s.rotateBy(degree);
					}
					if (!Float.isNaN(scaleX) && scaleX != 1) {
						view.getUndoManager().addEdit(new UndoScaleManipulable(view, scaleX, 0));
						s.scaleX(scaleX);
					}
					if (!Float.isNaN(scaleY) && scaleY != 1) {
						view.getUndoManager().addEdit(new UndoScaleManipulable(view, scaleY, 1));
						s.scaleY(scaleY);
					}
					if (!Float.isNaN(shearX) && shearX != 0) {
						view.getUndoManager().addEdit(new UndoShearManipulable(view, shearX, 0));
						s.shearX(shearX);
					}
					if (!Float.isNaN(shearY) && shearY != 0) {
						view.getUndoManager().addEdit(new UndoShearManipulable(view, shearY, 1));
						s.shearY(shearY);
					}
					if (flipXCheckBox.isSelected()) {
						view.getUndoManager().addEdit(new UndoFlipManipulable(view, 0));
						s.flipX();
					}
					if (flipYCheckBox.isSelected()) {
						view.getUndoManager().addEdit(new UndoFlipManipulable(view, 1));
						s.flipY();
					}
					if (s instanceof Blob2D) {
						((Blob2D) s).update();
					}
				} else if (shape instanceof Annulus) {
					if (!Float.isNaN(innerDiameter) && !Float.isNaN(outerDiameter)) {
						view.resizeManipulableTo(part, xcenter, view.model.getLy() - ycenter, innerDiameter, outerDiameter, 0, 0);
					}
				} else if (shape instanceof EllipticalAnnulus) {
					if (!Float.isNaN(innerA) && !Float.isNaN(innerB) && !Float.isNaN(outerA) && !Float.isNaN(outerB)) {
						view.resizeManipulableTo(part, xcenter, view.model.getLy() - ycenter, innerA, innerB, outerA, outerB);
					}
				}

				part.setWindAngle((float) Math.toRadians(windAngle));
				part.setWindSpeed(windSpeed);
				part.setThermalConductivity(Math.max(conductivity, 0.000000001f));
				part.setSpecificHeat(specificHeat);
				part.setDensity(density);
				part.setAbsorptivity(absorptivity);
				part.setReflectivity(reflectivity);
				part.setTransmissivity(transmissivity);
				part.setEmissivity(emissivity);
				part.setScattering(scattering);
				part.setScatteringVisible(visibleScattering);
				part.setElasticity(elasticity);
				part.setLabel(labelField.getText());
				part.setUid(uid);

				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();

				view.previousProperties.temperature = part.getTemperature();
				view.previousProperties.thermalConductivity = part.getThermalConductivity();
				view.previousProperties.specificHeat = part.getSpecificHeat();
				view.previousProperties.density = part.getDensity();
				view.previousProperties.absorptivity = part.getAbsorptivity();
				view.previousProperties.reflectivity = part.getReflectivity();
				view.previousProperties.transmissivity = part.getTransmissivity();
				view.previousProperties.emissivity = part.getEmissivity();
				view.previousProperties.elasticity = part.getElasticity();

				dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(button);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(tabbedPane, BorderLayout.CENTER);

		JPanel p = new JPanel(new SpringLayout());
		JPanel pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Geometry");
		int count = 0;

		p.add(new JLabel("Center x"));
		xField = new JTextField(FORMAT.format(part.getCenter().x));
		xField.addActionListener(okListener);
		p.add(xField);
		p.add(new JLabel(part.getShape() instanceof Polygon2D ? "<html><i>m</i> (" + ((Polygon2D) part.getShape()).getVertexCount() + " points)</html>" : "<html><i>m</i></html>"));

		p.add(new JLabel("Center y"));
		yField = new JTextField(FORMAT.format(view.model.getLy() - part.getCenter().y));
		yField.addActionListener(okListener);
		p.add(yField);
		p.add(new JLabel("<html><i>m</i></html>"));
		count++;

		Shape shape = part.getShape();
		if (shape instanceof RectangularShape) {

			p.add(new JLabel("Width"));
			wField = new JTextField(FORMAT.format(shape.getBounds2D().getWidth()));
			wField.addActionListener(okListener);
			p.add(wField);
			p.add(new JLabel("<html><i>m</i></html>"));

			p.add(new JLabel("Height"));
			hField = new JTextField(FORMAT.format(shape.getBounds2D().getHeight()));
			hField.addActionListener(okListener);
			p.add(hField);
			p.add(new JLabel("<html><i>m</i></html>"));
			count++;

		} else if (shape instanceof TransformableShape) {

			p.add(new JLabel("Rotate"));
			angleField = new JTextField("0");
			angleField.addActionListener(okListener);
			p.add(angleField);
			p.add(new JLabel("<html>&deg;</html>"));

			p.add(new JLabel("Flip"));
			flipXCheckBox = new JCheckBox("Horizontal");
			p.add(flipXCheckBox);
			flipYCheckBox = new JCheckBox("Vertical");
			p.add(flipYCheckBox);
			count++;

			p.add(new JLabel("Shear X"));
			shearXField = new JTextField("0");
			shearXField.addActionListener(okListener);
			p.add(shearXField);
			p.add(new JLabel());

			p.add(new JLabel("Shear Y"));
			shearYField = new JTextField("0");
			shearYField.addActionListener(okListener);
			p.add(shearYField);
			p.add(new JLabel());
			count++;

			p.add(new JLabel("Scale X"));
			scaleXField = new JTextField("1");
			scaleXField.addActionListener(okListener);
			p.add(scaleXField);
			p.add(new JLabel("Must be > 0"));

			p.add(new JLabel("Scale Y"));
			scaleYField = new JTextField("1");
			scaleYField.addActionListener(okListener);
			p.add(scaleYField);
			p.add(new JLabel("Must be > 0"));
			count++;

		} else if (shape instanceof Annulus) {

			Annulus ring = (Annulus) shape;

			p.add(new JLabel("Inner diameter"));
			innerDiameterField = new JTextField(FORMAT.format(ring.getInnerDiameter()));
			innerDiameterField.addActionListener(okListener);
			p.add(innerDiameterField);
			p.add(new JLabel("<html><i>m</i></html>"));

			p.add(new JLabel("Outer diameter"));
			outerDiameterField = new JTextField(FORMAT.format(ring.getOuterDiameter()));
			outerDiameterField.addActionListener(okListener);
			p.add(outerDiameterField);
			p.add(new JLabel("<html><i>m</i></html>"));
			count++;

		} else if (shape instanceof EllipticalAnnulus) {

			EllipticalAnnulus annulus = (EllipticalAnnulus) shape;

			p.add(new JLabel("Inner a"));
			innerAField = new JTextField(FORMAT.format(annulus.getInnerA()));
			innerAField.addActionListener(okListener);
			p.add(innerAField);
			p.add(new JLabel("<html><i>m</i></html>"));

			p.add(new JLabel("Inner b"));
			innerBField = new JTextField(FORMAT.format(annulus.getInnerB()));
			innerBField.addActionListener(okListener);
			p.add(innerBField);
			p.add(new JLabel("<html><i>m</i></html>"));
			count++;

			p.add(new JLabel("Outer a"));
			outerAField = new JTextField(FORMAT.format(annulus.getOuterA()));
			outerAField.addActionListener(okListener);
			p.add(outerAField);
			p.add(new JLabel("<html><i>m</i></html>"));

			p.add(new JLabel("Outer b"));
			outerBField = new JTextField(FORMAT.format(annulus.getOuterB()));
			outerBField.addActionListener(okListener);
			p.add(outerBField);
			p.add(new JLabel("<html><i>m</i></html>"));
			count++;

		}

		MiscUtil.makeCompactGrid(p, count, 6, 5, 5, 10, 2);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Source");

		ButtonGroup bg = new ButtonGroup();
		notHeatSourceRadioButton = new JRadioButton("Not a heat source");
		notHeatSourceRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setText("Power density");
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
					powerField.setText("0");
				}
			}
		});
		p.add(notHeatSourceRadioButton);
		bg.add(notHeatSourceRadioButton);

		constantTemperatureRadioButton = new JRadioButton("Constant temperature");
		constantTemperatureRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setText("Power density");
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
					powerField.setText("0");
				}
			}
		});
		p.add(constantTemperatureRadioButton);
		bg.add(constantTemperatureRadioButton);

		powerRadioButton = new JRadioButton("Power Source");
		powerRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(false);
					temperatureField.setEnabled(false);
					powerLabel.setText("<html><u><font color=blue>Power density</font></u></html>");
					powerLabel.setEnabled(true);
					powerField.setEnabled(true);
					powerField.setText(FORMAT.format(part.getPower()));
				}
			}
		});
		p.add(powerRadioButton);
		bg.add(powerRadioButton);

		p = new JPanel(new SpringLayout());
		pp.add(p, BorderLayout.CENTER);
		count = 0;

		temperatureLabel = new JLabel("Temperature");
		p.add(temperatureLabel);
		temperatureField = new JTextField(FORMAT.format(part.getTemperature()), 16);
		temperatureField.addActionListener(okListener);
		p.add(temperatureField);
		p.add(new JLabel("<html><i>\u2103</i></html>"));
		count++;

		powerLabel = new JLabel("Power density");
		powerLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (powerLabel.isEnabled()) {
					if (powerRadioButton.isSelected())
						new ThermostatDialog(view, part, true).setVisible(true);
				}
			}

			public void mouseEntered(MouseEvent e) {
				if (powerLabel.isEnabled()) {
					powerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					if (powerRadioButton.isSelected())
						powerLabel.setToolTipText(powerLabel.isEnabled() ? "Click to set up a thermostat" : null);
				}
			}

			public void mouseExited(MouseEvent e) {
				if (powerLabel.isEnabled())
					powerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		p.add(powerLabel);
		powerField = new JTextField(FORMAT.format(part.getPower()), 16);
		powerField.addActionListener(okListener);
		p.add(powerField);
		p.add(new JLabel("<html><i>W/m<sup><font size=2>3</font></sup></html>"));
		count++;

		p.add(new JLabel("Temperature Coefficient"));
		temperatureCoefficientField = new JTextField(part.getThermistorTemperatureCoefficient() + "", 16);
		temperatureCoefficientField.addActionListener(okListener);
		p.add(temperatureCoefficientField);
		p.add(new JLabel("<html><i>1/&deg;C</i></html>"));
		count++;

		p.add(new JLabel("Reference Temperature"));
		referenceTemperatureField = new JTextField(part.getThermistorReferenceTemperature() + "", 16);
		referenceTemperatureField.addActionListener(okListener);
		p.add(referenceTemperatureField);
		p.add(new JLabel("<html><i>&deg;C</i></html>"));
		count++;

		p.add(new JLabel("Wind speed"));
		windSpeedField = new JTextField(FORMAT.format(part.getWindSpeed()), 8);
		windSpeedField.addActionListener(okListener);
		p.add(windSpeedField);
		p.add(new JLabel("<html><i>m/s</i></html>"));
		count++;

		p.add(new JLabel("Wind angle"));
		windAngleField = new JTextField(FORMAT.format(Math.toDegrees(part.getWindAngle())), 8);
		windAngleField.addActionListener(okListener);
		p.add(windAngleField);
		p.add(new JLabel("Degrees"));
		count++;

		if (part.getPower() != 0) {
			powerRadioButton.setSelected(true);
		} else if (part.getConstantTemperature()) {
			constantTemperatureRadioButton.setSelected(true);
		} else {
			notHeatSourceRadioButton.setSelected(true);
		}

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Thermal");
		count = 0;

		p.add(new JLabel("Thermal conductivity"));
		thermalConductivityField = new JTextField(FORMAT.format(part.getThermalConductivity()), 8);
		thermalConductivityField.addActionListener(okListener);
		p.add(thermalConductivityField);
		p.add(new JLabel("<html><i>W/(m\u00b7\u2103)</i></html>"));
		count++;

		p.add(new JLabel("Specific heat"));
		specificHeatField = new JTextField(FORMAT.format(part.getSpecificHeat()), 8);
		specificHeatField.addActionListener(okListener);
		p.add(specificHeatField);
		p.add(new JLabel("<html><i>J/(kg\u00b7\u2103)</i></html>"));
		count++;

		p.add(new JLabel("Density"));
		densityField = new JTextField(FORMAT.format(part.getDensity()), 8);
		densityField.addActionListener(okListener);
		p.add(densityField);
		p.add(new JLabel("<html><i>kg/m<sup><font size=2>3</font></sup></html>"));
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		// optics

		p = new JPanel(new GridLayout(2, 2, 8, 8));
		p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Optical");

		p.add(new JLabel("Absorptivity:"));
		absorptivityField = new JTextField(FORMAT.format(part.getAbsorptivity()), 10);
		absorptivityField.addActionListener(okListener);
		p.add(absorptivityField);

		p.add(new JLabel("Reflectivity:"));
		reflectivityField = new JTextField(FORMAT.format(part.getReflectivity()), 10);
		reflectivityField.addActionListener(okListener);
		p.add(reflectivityField);

		p.add(new JLabel("Transmissivity:"));
		transmissivityField = new JTextField(FORMAT.format(part.getTransmissivity()), 10);
		transmissivityField.addActionListener(okListener);
		p.add(transmissivityField);

		p.add(new JLabel("Emissivity:"));
		emissivityField = new JTextField(FORMAT.format(part.getEmissivity()), 10);
		emissivityField.addActionListener(okListener);
		p.add(emissivityField);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("For light particles only"));
		pp.add(p, BorderLayout.CENTER);

		bg = new ButtonGroup();
		noScatteringRadioButton = new JRadioButton("No scattering", !part.getScattering());
		p.add(noScatteringRadioButton);
		bg.add(noScatteringRadioButton);

		visibleScatteringRadioButton = new JRadioButton("Scattering (visible)", part.getScattering() && part.isScatteringVisible());
		p.add(visibleScatteringRadioButton);
		bg.add(visibleScatteringRadioButton);

		invisibleScatteringRadioButton = new JRadioButton("Scattering (invisible)", part.getScattering() && !part.isScatteringVisible());
		p.add(invisibleScatteringRadioButton);
		bg.add(invisibleScatteringRadioButton);

		// mechanics

		p = new JPanel(new GridLayout(1, 3, 8, 8));
		p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Mechanical");

		p.add(new JLabel("Elasticity:"));
		elasticityField = new JTextField(FORMAT.format(part.getElasticity()), 10);
		elasticityField.addActionListener(okListener);
		p.add(elasticityField);
		p.add(new JLabel("Dimensionless [0, 1]"));

		// miscellaneous

		Box miscBox = Box.createVerticalBox();
		pp = new JPanel(new BorderLayout());
		pp.add(miscBox, BorderLayout.NORTH);
		tabbedPane.add(pp, "Miscellaneous");

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(part.getUid(), 20);
		uidField.addActionListener(okListener);
		p.add(uidField);
		p.add(new JLabel("Label:"));
		labelField = new JTextField(part.getLabel(), 20);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		p.add(new JLabel("<html><br><hr align=left width=100>1) Set a unique ID if you need to find this part in scripts.<br>2) The label will be shown on top of this part in the view.</html>"));

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}
