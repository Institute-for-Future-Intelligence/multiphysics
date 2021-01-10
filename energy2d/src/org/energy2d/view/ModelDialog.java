package org.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.energy2d.event.ManipulationEvent;
import org.energy2d.model.Boundary;
import org.energy2d.model.DirichletThermalBoundary;
import org.energy2d.model.MassBoundary;
import org.energy2d.model.SimpleMassBoundary;
import org.energy2d.model.ThermalBoundary;
import org.energy2d.model.Model2D;
import org.energy2d.model.NeumannThermalBoundary;
import org.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ModelDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.#########");

	private JTextField steplengthField;
	private JTextField bgTemperatureField;
	private JTextField conductivityField;
	private JTextField specificHeatField;
	private JTextField densityField;
	private JLabel viscosityLabel;
	private JTextField viscosityField;
	private JLabel thermalExpansionCoefficientLabel;
	private JTextField thermalExpansionCoefficientField;
	private JTextField wField, hField;
	private JTabbedPane boundaryTab;
	private JLabel upperThermalBoundaryLabel;
	private JLabel lowerThermalBoundaryLabel;
	private JLabel leftThermalBoundaryLabel;
	private JLabel rightThermalBoundaryLabel;
	private JLabel upperThermalBoundaryLabel2;
	private JLabel lowerThermalBoundaryLabel2;
	private JLabel leftThermalBoundaryLabel2;
	private JLabel rightThermalBoundaryLabel2;
	private JTextField upperThermalBoundaryField;
	private JTextField lowerThermalBoundaryField;
	private JTextField leftThermalBoundaryField;
	private JTextField rightThermalBoundaryField;
	private JRadioButton upperMassBoundaryReflect;
	private JRadioButton lowerMassBoundaryReflect;
	private JRadioButton leftMassBoundaryReflect;
	private JRadioButton rightMassBoundaryReflect;
	private JRadioButton upperMassBoundaryThrough;
	private JRadioButton lowerMassBoundaryThrough;
	private JRadioButton leftMassBoundaryThrough;
	private JRadioButton rightMassBoundaryThrough;
	private JRadioButton upperMassBoundaryStop;
	private JRadioButton lowerMassBoundaryStop;
	private JRadioButton leftMassBoundaryStop;
	private JRadioButton rightMassBoundaryStop;
	private JLabel solarPowerLabel;
	private JTextField solarPowerField;
	private JLabel raySpeedLabel;
	private JTextField raySpeedField;
	private JLabel rayNumberLabel;
	private JTextField rayNumberField;
	private JLabel emissionIntervalLabel;
	private JTextField emissionIntervalField;
	private JComboBox<String> thermalBoundaryComboBox;
	private JLabel sunAngleLabel;
	private JSlider sunAngleSlider;
	private JCheckBox sunnyCheckBox;
	private JCheckBox convectiveCheckBox;
	private JLabel buoyancyApproximationLabel;
	private JComboBox<String> buoyancyApproximationComboBox;
	private JLabel gravityTypeLabel;
	private JComboBox<String> gravityTypeComboBox;
	private JTextField zDiffusivityField;
	private JCheckBox zDiffusivityOnlyForFluidCheckBox;
	private JTextField gravitationalAccelerationField;
	private JTextField particleDragField;
	private JTextField particleHardnessField;
	private JTextField thermophoreticField;
	private Window owner;
	private ActionListener okListener;

	ModelDialog(final View2D view, final Model2D model, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Model Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		okListener = e -> {

			float bgTemperature = parse(bgTemperatureField.getText());
			if (Float.isNaN(bgTemperature))
				return;
			float conductivity = parse(conductivityField.getText());
			if (Float.isNaN(conductivity))
				return;
			float specificHeat = parse(specificHeatField.getText());
			if (Float.isNaN(specificHeat))
				return;
			float density = parse(densityField.getText());
			if (Float.isNaN(density))
				return;
			float viscosity = parse(viscosityField.getText());
			if (Float.isNaN(viscosity))
				return;
			float thermalExpansionCoefficient = parse(thermalExpansionCoefficientField.getText());
			if (Float.isNaN(thermalExpansionCoefficient))
				return;
			float steplength = parse(steplengthField.getText());
			if (Float.isNaN(steplength))
				return;
			float width = parse(wField.getText());
			if (Float.isNaN(width))
				return;
			float height = parse(hField.getText());
			if (Float.isNaN(height))
				return;
			float valueAtLeft = parse(leftThermalBoundaryField.getText());
			if (Float.isNaN(valueAtLeft))
				return;
			float valueAtRight = parse(rightThermalBoundaryField.getText());
			if (Float.isNaN(valueAtRight))
				return;
			float valueAtUpper = parse(upperThermalBoundaryField.getText());
			if (Float.isNaN(valueAtUpper))
				return;
			float valueAtLower = parse(lowerThermalBoundaryField.getText());
			if (Float.isNaN(valueAtLower))
				return;
			float solarPower = parse(solarPowerField.getText());
			if (Float.isNaN(solarPower))
				return;
			float raySpeed = parse(raySpeedField.getText());
			if (Float.isNaN(raySpeed))
				return;
			float rayNumber = parse(rayNumberField.getText());
			if (Float.isNaN(rayNumber))
				return;
			float emissionInterval = parse(emissionIntervalField.getText());
			if (Float.isNaN(emissionInterval))
				return;
			float zHeatDiffusivity = parse(zDiffusivityField.getText());
			if (Float.isNaN(zHeatDiffusivity))
				return;
			float gravitationalAcceleration = parse(gravitationalAccelerationField.getText());
			if (Float.isNaN(gravitationalAcceleration))
				return;
			float thermophoreticCoefficient = parse(thermophoreticField.getText());
			if (Float.isNaN(thermophoreticCoefficient))
				return;
			float particleDrag = parse(particleDragField.getText());
			if (Float.isNaN(particleDrag))
				return;
			float particleHardness = parse(particleHardnessField.getText());
			if (Float.isNaN(particleHardness))
				return;

			if (steplength <= 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Time step must be greater than zero!", "Time step input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (density <= 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Medium density must be greater than zero!", "Density input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (specificHeat <= 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Medium specific heat must be greater than zero!", "Specific heat input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (thermophoreticCoefficient < 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Thermophoretic coefficient cannot be negative!", "Thermophoretic cofficient input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (particleDrag < 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Particle drag cannot be negative!", "Drag input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (particleHardness < 0) {
				JOptionPane.showMessageDialog(ModelDialog.this, "Particle hardness cannot be negative!", "Particle hardness input error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			model.setTimeStep(steplength);
			model.setBackgroundTemperature(bgTemperature);
			model.setBackgroundConductivity(Math.max(conductivity, 0.000000001f));
			model.setBackgroundSpecificHeat(specificHeat);
			model.setBackgroundDensity(density);
			model.setBackgroundViscosity(viscosity);
			model.setThermalExpansionCoefficient(thermalExpansionCoefficient);
			float dx = width - model.getLx();
			float dy = height - model.getLy();
			if (dx != 0)
				model.setLx(width);
			if (dy != 0) {
				model.setLy(height);
				model.translateAllBy(0, dy); // fix the y-flip problem
			}
			view.setArea(0, width, 0, height);
			model.setSolarPowerDensity(solarPower);
			model.setSolarRaySpeed(raySpeed);
			model.setSolarRayCount((int) rayNumber);
			model.setPhotonEmissionInterval((int) emissionInterval);
			model.setSunAngle((float) Math.toRadians(sunAngleSlider.getValue()));
			model.setZHeatDiffusivity(zHeatDiffusivity);
			model.setZHeatDiffusivityOnlyForFluid(zDiffusivityOnlyForFluidCheckBox.isSelected());
			model.setGravitationalAcceleration(gravitationalAcceleration);
			model.setThermophoreticCoefficient(thermophoreticCoefficient);
			model.setParticleDrag(particleDrag);
			model.setParticleHardness(particleHardness * 0.000001f);

			switch (thermalBoundaryComboBox.getSelectedIndex()) {
			case 0:
				DirichletThermalBoundary dhb = new DirichletThermalBoundary();
				dhb.setTemperatureAtBorder(Boundary.LEFT, valueAtLeft);
				dhb.setTemperatureAtBorder(Boundary.RIGHT, valueAtRight);
				dhb.setTemperatureAtBorder(Boundary.UPPER, valueAtUpper);
				dhb.setTemperatureAtBorder(Boundary.LOWER, valueAtLower);
				model.setThermalBoundary(dhb);
				break;
			case 1:
				NeumannThermalBoundary nhb = new NeumannThermalBoundary();
				nhb.setFluxAtBorder(Boundary.LEFT, valueAtLeft);
				nhb.setFluxAtBorder(Boundary.RIGHT, valueAtRight);
				nhb.setFluxAtBorder(Boundary.UPPER, valueAtUpper);
				nhb.setFluxAtBorder(Boundary.LOWER, valueAtLower);
				model.setThermalBoundary(nhb);
				break;
			}

			SimpleMassBoundary massBoundary = (SimpleMassBoundary) model.getMassBoundary();
			if (leftMassBoundaryReflect.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LEFT, MassBoundary.REFLECTIVE);
			} else if (leftMassBoundaryStop.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LEFT, MassBoundary.STOP);
			} else if (leftMassBoundaryThrough.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LEFT, MassBoundary.THROUGH);
			}
			if (rightMassBoundaryReflect.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.RIGHT, MassBoundary.REFLECTIVE);
			} else if (rightMassBoundaryStop.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.RIGHT, MassBoundary.STOP);
			} else if (rightMassBoundaryThrough.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.RIGHT, MassBoundary.THROUGH);
			}
			if (upperMassBoundaryReflect.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.UPPER, MassBoundary.REFLECTIVE);
			} else if (upperMassBoundaryStop.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.UPPER, MassBoundary.STOP);
			} else if (upperMassBoundaryThrough.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.UPPER, MassBoundary.THROUGH);
			}
			if (lowerMassBoundaryReflect.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LOWER, MassBoundary.REFLECTIVE);
			} else if (lowerMassBoundaryStop.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LOWER, MassBoundary.STOP);
			} else if (lowerMassBoundaryThrough.isSelected()) {
				massBoundary.setFlowTypeAtBorder(Boundary.LOWER, MassBoundary.THROUGH);
			}

			model.setSunny(sunnyCheckBox.isSelected());
			model.setConvective(convectiveCheckBox.isSelected());
			model.setBuoyancyApproximation((byte) buoyancyApproximationComboBox.getSelectedIndex());
			model.setGravityType((byte) gravityTypeComboBox.getSelectedIndex());

			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();

			view.repaint();

			if (!(e.getSource() instanceof JComboBox))
				dispose();

			view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);

		};

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(e -> dispose());
		buttonPanel.add(button);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(tabbedPane, BorderLayout.CENTER);

		// general properties pane

		JPanel p = new JPanel(new SpringLayout());
		JPanel pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "General");
		int count = 0;

		convectiveCheckBox = new JCheckBox("Convective");
		convectiveCheckBox.setSelected(model.isConvective());
		convectiveCheckBox.addItemListener(e -> {
			boolean b = convectiveCheckBox.isSelected();
			viscosityLabel.setEnabled(b);
			viscosityField.setEnabled(b);
			thermalExpansionCoefficientLabel.setEnabled(b);
			thermalExpansionCoefficientField.setEnabled(b);
			buoyancyApproximationLabel.setEnabled(b);
			buoyancyApproximationComboBox.setEnabled(b);
			gravityTypeLabel.setEnabled(b);
			gravityTypeComboBox.setEnabled(b);
		});
		p.add(convectiveCheckBox);

		sunnyCheckBox = new JCheckBox("Sunny");
		sunnyCheckBox.setSelected(model.isSunny());
		sunnyCheckBox.addItemListener(e -> {
			boolean b = sunnyCheckBox.isSelected();
			sunAngleSlider.setEnabled(b);
			emissionIntervalLabel.setEnabled(b);
			emissionIntervalField.setEnabled(b);
			rayNumberLabel.setEnabled(b);
			rayNumberField.setEnabled(b);
			raySpeedLabel.setEnabled(b);
			raySpeedField.setEnabled(b);
			solarPowerLabel.setEnabled(b);
			solarPowerField.setEnabled(b);
		});
		p.add(sunnyCheckBox);

		// dummy
		JLabel label = new JLabel();
		p.add(label);

		label = new JLabel("Width");
		p.add(label);
		wField = new JTextField(FORMAT.format(model.getLx()), 8);
		wField.addActionListener(okListener);
		p.add(wField);
		label = new JLabel("<html><i>m</i></html>");
		p.add(label);
		count++;

		label = new JLabel("Time steplength");
		p.add(label);
		steplengthField = new JTextField(FORMAT.format(model.getTimeStep()), 8);
		steplengthField.addActionListener(okListener);
		p.add(steplengthField);
		label = new JLabel("<html><i>s</i></html>");
		p.add(label);

		label = new JLabel("Height");
		p.add(label);
		hField = new JTextField(FORMAT.format(model.getLy()), 8);
		hField.addActionListener(okListener);
		p.add(hField);
		label = new JLabel("<html><i>m</i></html>");
		p.add(label);
		count++;

		label = new JLabel("Z heat diffusivity *");
		p.add(label);
		zDiffusivityField = new JTextField(FORMAT.format(model.getZHeatDiffusivity()), 8);
		zDiffusivityField.addActionListener(okListener);
		p.add(zDiffusivityField);

		zDiffusivityOnlyForFluidCheckBox = new JCheckBox("Fluid only", model.getZHeatDiffusivityOnlyForFluid());
		zDiffusivityOnlyForFluidCheckBox.setToolTipText("Apply Z heat diffusivity only to fluid");
		p.add(zDiffusivityOnlyForFluidCheckBox);

		// dummy
		label = new JLabel();
		p.add(label);

		// dummy
		label = new JLabel();
		p.add(label);

		// dummy
		label = new JLabel();
		p.add(label);

		count++;

		MiscUtil.makeCompactGrid(p, count, 6, 5, 5, 10, 2);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pp.add(p, BorderLayout.CENTER);
		p.add(new JLabel("<html><br><hr align=left width=100>* The Z heat diffusivity permits heat exchange in the direction perpendicular to the screen.<br>If it is positive, the system will settle at the background temperature set for the medium.<br>This property can be applied to fluid only.</html>"));

		// fluid properties pane

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Medium");
		count = 0;

		label = new JLabel("Background temperature");
		p.add(label);
		bgTemperatureField = new JTextField(FORMAT.format(model.getBackgroundTemperature()), 16);
		bgTemperatureField.addActionListener(okListener);
		p.add(bgTemperatureField);
		label = new JLabel("<html><i>\u2103</i></html>");
		p.add(label);
		count++;

		label = new JLabel("Conductivity");
		p.add(label);
		conductivityField = new JTextField(FORMAT.format(model.getBackgroundConductivity()), 16);
		conductivityField.addActionListener(okListener);
		p.add(conductivityField);
		label = new JLabel("<html><i>W/(m\u00b7\u2103)</i></html>");
		p.add(label);
		count++;

		label = new JLabel("Specific heat");
		p.add(label);
		specificHeatField = new JTextField(FORMAT.format(model.getBackgroundSpecificHeat()), 16);
		specificHeatField.addActionListener(okListener);
		p.add(specificHeatField);
		label = new JLabel("<html><i>J/(kg\u00b7\u2103)</i></html>");
		p.add(label);
		count++;

		label = new JLabel("Density");
		p.add(label);
		densityField = new JTextField(FORMAT.format(model.getBackgroundDensity()), 16);
		densityField.addActionListener(okListener);
		p.add(densityField);
		label = new JLabel("<html><i>kg/m<sup><font size=2>3</font></sup></html>");
		p.add(label);
		count++;

		viscosityLabel = new JLabel("Kinematic viscosity");
		viscosityLabel.setEnabled(model.isConvective());
		p.add(viscosityLabel);
		viscosityField = new JTextField(FORMAT.format(model.getBackgroundViscosity()), 16);
		viscosityField.setEnabled(model.isConvective());
		viscosityField.addActionListener(okListener);
		p.add(viscosityField);
		label = new JLabel("<html><i>m<sup><font size=2>2</font></sup>/s</html>");
		p.add(label);
		count++;

		thermalExpansionCoefficientLabel = new JLabel("Thermal expansion coefficient");
		thermalExpansionCoefficientLabel.setEnabled(model.isConvective());
		p.add(thermalExpansionCoefficientLabel);
		thermalExpansionCoefficientField = new JTextField(FORMAT.format(model.getThermalExpansionCoefficient()), 16);
		thermalExpansionCoefficientField.setEnabled(model.isConvective());
		thermalExpansionCoefficientField.addActionListener(okListener);
		p.add(thermalExpansionCoefficientField);
		label = new JLabel("<html><i>m/(s<sup><font size=2>2</font></sup>\u00b7\u2103)</html>)");
		p.add(label);
		count++;

		buoyancyApproximationLabel = new JLabel("Buoyancy approximation");
		buoyancyApproximationLabel.setEnabled(model.isConvective());
		p.add(buoyancyApproximationLabel);
		buoyancyApproximationComboBox = new JComboBox<String>(new String[] { "All-cell average", "Column average" });
		buoyancyApproximationComboBox.setEnabled(model.isConvective());
		buoyancyApproximationComboBox.setSelectedIndex(model.getBuoyancyApproximation());
		p.add(buoyancyApproximationComboBox);
		label = new JLabel();
		p.add(label);
		count++;

		gravityTypeLabel = new JLabel("Gravity type");
		gravityTypeLabel.setEnabled(model.isConvective());
		p.add(gravityTypeLabel);
		gravityTypeComboBox = new JComboBox<String>(new String[] { "Uniform", "Centric" });
		gravityTypeComboBox.setEnabled(model.isConvective());
		gravityTypeComboBox.setSelectedIndex(model.getGravityType());
		p.add(gravityTypeComboBox);
		label = new JLabel();
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		pp.add(new JLabel("<html><br><hr align=left width=100>* The above parameters set up a photon (light particle) simulation of sunlight.</html>"), BorderLayout.CENTER);
		tabbedPane.add(pp, "Sunlight");
		count = 0;

		rayNumberLabel = new JLabel("Ray number");
		rayNumberLabel.setEnabled(model.isSunny());
		p.add(rayNumberLabel);
		rayNumberField = new JTextField(FORMAT.format(model.getSolarRayCount()), 16);
		rayNumberField.setEnabled(model.isSunny());
		rayNumberField.addActionListener(okListener);
		p.add(rayNumberField);
		label = new JLabel();
		p.add(label);
		count++;

		raySpeedLabel = new JLabel("Ray speed");
		raySpeedLabel.setEnabled(model.isSunny());
		p.add(raySpeedLabel);
		raySpeedField = new JTextField(FORMAT.format(model.getSolarRaySpeed()), 16);
		raySpeedField.setEnabled(model.isSunny());
		raySpeedField.addActionListener(okListener);
		p.add(raySpeedField);
		label = new JLabel("<html><i>m/s</i></html>");
		p.add(label);
		count++;

		emissionIntervalLabel = new JLabel("Emission interval");
		emissionIntervalLabel.setEnabled(model.isSunny());
		p.add(emissionIntervalLabel);
		emissionIntervalField = new JTextField(FORMAT.format(model.getPhotonEmissionInterval()), 16);
		emissionIntervalField.setEnabled(model.isSunny());
		emissionIntervalField.addActionListener(okListener);
		p.add(emissionIntervalField);
		label = new JLabel();
		p.add(label);
		count++;

		solarPowerLabel = new JLabel("Solar power density");
		solarPowerLabel.setEnabled(model.isSunny());
		p.add(solarPowerLabel);
		solarPowerField = new JTextField(FORMAT.format(model.getSolarPowerDensity()), 16);
		solarPowerField.setEnabled(model.isSunny());
		solarPowerField.addActionListener(okListener);
		p.add(solarPowerField);
		label = new JLabel("<html><i>W/m<sup><font size=2>3</font></sup></html>)");
		p.add(label);
		count++;

		sunAngleLabel = new JLabel("<html>Sun angle<br>(Dawn to dusk)</html>");
		sunAngleLabel.setEnabled(model.isSunny());
		p.add(sunAngleLabel);
		int angle = (int) Math.toDegrees(model.getSunAngle());
		if (angle < 0)
			angle = 0;
		else if (angle > 180)
			angle = 180;
		sunAngleSlider = new JSlider(0, 180, angle);
		sunAngleSlider.setEnabled(model.isSunny());
		sunAngleSlider.setPaintTicks(true);
		sunAngleSlider.setMajorTickSpacing(45);
		sunAngleSlider.setMinorTickSpacing(15);
		sunAngleSlider.setPaintLabels(true);
		Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
		ht.put(0, new JLabel("0\u00b0"));
		ht.put(45, new JLabel("45\u00b0"));
		ht.put(90, new JLabel("90\u00b0"));
		ht.put(135, new JLabel("135\u00b0"));
		ht.put(180, new JLabel("180\u00b0"));
		sunAngleSlider.setLabelTable(ht);
		p.add(sunAngleSlider);
		label = new JLabel("Degree");
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Particle");
		count = 0;

		p.add(new JLabel("Particle Count"));
		JTextField particleCountField = new JTextField("" + model.getParticles().size());
		particleCountField.setEditable(false);
		p.add(particleCountField);
		p.add(new JLabel());
		count++;

		p.add(new JLabel("Gravitational Acceleration"));
		gravitationalAccelerationField = new JTextField("" + model.getGravitationalAcceleration(), 16);
		gravitationalAccelerationField.addActionListener(okListener);
		p.add(gravitationalAccelerationField);
		label = new JLabel("<html>m/s<sup>2</sup></html>");
		p.add(label);
		count++;

		p.add(new JLabel("Drag Coefficient"));
		particleDragField = new JTextField(FORMAT.format(model.getParticleDrag()), 16);
		particleDragField.addActionListener(okListener);
		p.add(particleDragField);
		label = new JLabel("kg/s");
		p.add(label);
		count++;

		p.add(new JLabel("Hardness"));
		particleHardnessField = new JTextField(FORMAT.format(model.getParticleHardness() * 1000000f), 16);
		particleHardnessField.addActionListener(okListener);
		p.add(particleHardnessField);
		label = new JLabel("");
		p.add(label);
		count++;

		p.add(new JLabel("Thermophoretic Coefficient"));
		thermophoreticField = new JTextField(FORMAT.format(model.getThermophoreticCoefficient()), 16);
		thermophoreticField.addActionListener(okListener);
		p.add(thermophoreticField);
		label = new JLabel("<html>kg<sup>2</sup>m<sup>2</sup>/s<sup>2</sup></html>");
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		boundaryTab = new JTabbedPane();
		tabbedPane.add(boundaryTab, "Boundary");

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		boundaryTab.add(pp, "Thermal Boundary");
		count = 0;

		label = new JLabel("Thermal boundary condition");
		p.add(label);
		thermalBoundaryComboBox = new JComboBox<>(new String[]{"Dirichlet (constant temperature)", "Neumann (constant heat flux)", "Other"});
		if (model.getThermalBoundary() instanceof DirichletThermalBoundary) {
			thermalBoundaryComboBox.setSelectedIndex(0);
		} else if (model.getThermalBoundary() instanceof NeumannThermalBoundary) {
			thermalBoundaryComboBox.setSelectedIndex(1);
		} else {
			thermalBoundaryComboBox.setSelectedIndex(2);
		}
		thermalBoundaryComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				switch (thermalBoundaryComboBox.getSelectedIndex()) {
				case 0:
					setThermalBoundaryFields(new DirichletThermalBoundary());
					break;
				case 1:
					setThermalBoundaryFields(new NeumannThermalBoundary());
					break;
				case 2:
					enableBoundaryFieldsAndLabels(false);
					break;
				}
			}
		});
		thermalBoundaryComboBox.addActionListener(okListener);
		p.add(thermalBoundaryComboBox);
		label = new JLabel();
		p.add(label);
		count++;

		upperThermalBoundaryLabel = new JLabel();
		p.add(upperThermalBoundaryLabel);
		upperThermalBoundaryField = new JTextField();
		upperThermalBoundaryField.addActionListener(okListener);
		p.add(upperThermalBoundaryField);
		upperThermalBoundaryLabel2 = new JLabel();
		p.add(upperThermalBoundaryLabel2);
		count++;

		rightThermalBoundaryLabel = new JLabel();
		p.add(rightThermalBoundaryLabel);
		rightThermalBoundaryField = new JTextField();
		rightThermalBoundaryField.addActionListener(okListener);
		p.add(rightThermalBoundaryField);
		rightThermalBoundaryLabel2 = new JLabel();
		p.add(rightThermalBoundaryLabel2);
		count++;

		lowerThermalBoundaryLabel = new JLabel();
		p.add(lowerThermalBoundaryLabel);
		lowerThermalBoundaryField = new JTextField();
		lowerThermalBoundaryField.addActionListener(okListener);
		p.add(lowerThermalBoundaryField);
		lowerThermalBoundaryLabel2 = new JLabel();
		p.add(lowerThermalBoundaryLabel2);
		count++;

		leftThermalBoundaryLabel = new JLabel();
		p.add(leftThermalBoundaryLabel);
		leftThermalBoundaryField = new JTextField();
		leftThermalBoundaryField.addActionListener(okListener);
		p.add(leftThermalBoundaryField);
		leftThermalBoundaryLabel2 = new JLabel();
		p.add(leftThermalBoundaryLabel2);
		count++;

		setThermalBoundaryFields(model.getThermalBoundary());
		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		boundaryTab.add(pp, "Mass Boundary");
		count = 0;

		SimpleMassBoundary massBoundary = (SimpleMassBoundary) model.getMassBoundary();

		label = new JLabel("Upper boundary");
		p.add(label);
		upperMassBoundaryReflect = new JRadioButton("Reflect");
		p.add(upperMassBoundaryReflect);
		upperMassBoundaryStop = new JRadioButton("Stop");
		p.add(upperMassBoundaryStop);
		upperMassBoundaryThrough = new JRadioButton("Through");
		p.add(upperMassBoundaryThrough);
		ButtonGroup bg = new ButtonGroup();
		bg.add(upperMassBoundaryReflect);
		bg.add(upperMassBoundaryThrough);
		bg.add(upperMassBoundaryStop);
		switch (massBoundary.getFlowTypeAtBorder(Boundary.UPPER)) {
		case MassBoundary.REFLECTIVE:
			upperMassBoundaryReflect.setSelected(true);
			break;
		case MassBoundary.THROUGH:
			upperMassBoundaryThrough.setSelected(true);
			break;
		case MassBoundary.STOP:
			upperMassBoundaryStop.setSelected(true);
			break;
		}
		count++;

		label = new JLabel("Right boundary");
		p.add(label);
		rightMassBoundaryReflect = new JRadioButton("Reflect");
		p.add(rightMassBoundaryReflect);
		rightMassBoundaryStop = new JRadioButton("Stop");
		p.add(rightMassBoundaryStop);
		rightMassBoundaryThrough = new JRadioButton("Through");
		p.add(rightMassBoundaryThrough);
		bg = new ButtonGroup();
		bg.add(rightMassBoundaryReflect);
		bg.add(rightMassBoundaryThrough);
		bg.add(rightMassBoundaryStop);
		switch (massBoundary.getFlowTypeAtBorder(Boundary.RIGHT)) {
		case MassBoundary.REFLECTIVE:
			rightMassBoundaryReflect.setSelected(true);
			break;
		case MassBoundary.THROUGH:
			rightMassBoundaryThrough.setSelected(true);
			break;
		case MassBoundary.STOP:
			rightMassBoundaryStop.setSelected(true);
			break;
		}
		count++;

		label = new JLabel("Lower boundary");
		p.add(label);
		lowerMassBoundaryReflect = new JRadioButton("Reflect");
		p.add(lowerMassBoundaryReflect);
		lowerMassBoundaryStop = new JRadioButton("Stop");
		p.add(lowerMassBoundaryStop);
		lowerMassBoundaryThrough = new JRadioButton("Through");
		p.add(lowerMassBoundaryThrough);
		bg = new ButtonGroup();
		bg.add(lowerMassBoundaryReflect);
		bg.add(lowerMassBoundaryThrough);
		bg.add(lowerMassBoundaryStop);
		switch (massBoundary.getFlowTypeAtBorder(Boundary.LOWER)) {
		case MassBoundary.REFLECTIVE:
			lowerMassBoundaryReflect.setSelected(true);
			break;
		case MassBoundary.THROUGH:
			lowerMassBoundaryThrough.setSelected(true);
			break;
		case MassBoundary.STOP:
			lowerMassBoundaryStop.setSelected(true);
			break;
		}
		count++;

		label = new JLabel("Left boundary");
		p.add(label);
		leftMassBoundaryReflect = new JRadioButton("Reflect");
		p.add(leftMassBoundaryReflect);
		leftMassBoundaryStop = new JRadioButton("Stop");
		p.add(leftMassBoundaryStop);
		leftMassBoundaryThrough = new JRadioButton("Through");
		p.add(leftMassBoundaryThrough);
		bg = new ButtonGroup();
		bg.add(leftMassBoundaryReflect);
		bg.add(leftMassBoundaryThrough);
		bg.add(leftMassBoundaryStop);
		switch (massBoundary.getFlowTypeAtBorder(Boundary.LEFT)) {
		case MassBoundary.REFLECTIVE:
			leftMassBoundaryReflect.setSelected(true);
			break;
		case MassBoundary.THROUGH:
			leftMassBoundaryThrough.setSelected(true);
			break;
		case MassBoundary.STOP:
			leftMassBoundaryStop.setSelected(true);
			break;
		}
		count++;

		MiscUtil.makeCompactGrid(p, count, 4, 5, 5, 10, 2);

		pack();
		setLocationRelativeTo(view);

	}

	private void setThermalBoundaryFields(ThermalBoundary heatBoundary) {
		if (heatBoundary instanceof DirichletThermalBoundary) {
			enableBoundaryFieldsAndLabels(true);
			DirichletThermalBoundary b = (DirichletThermalBoundary) heatBoundary;
			leftThermalBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.LEFT)));
			rightThermalBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.RIGHT)));
			upperThermalBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.UPPER)));
			lowerThermalBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.LOWER)));
			leftThermalBoundaryLabel.setText("Left boundary temperature");
			rightThermalBoundaryLabel.setText("Right boundary temperature");
			upperThermalBoundaryLabel.setText("Upper boundary temperature");
			lowerThermalBoundaryLabel.setText("Lower boundary temperature");
			leftThermalBoundaryLabel2.setText("<html><i>\u2103  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i></html>");
			rightThermalBoundaryLabel2.setText("<html><i>\u2103 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i></html>");
			upperThermalBoundaryLabel2.setText("<html><i>\u2103 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i></html>");
			lowerThermalBoundaryLabel2.setText("<html><i>\u2103 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i></html>");
		} else if (heatBoundary instanceof NeumannThermalBoundary) {
			enableBoundaryFieldsAndLabels(true);
			NeumannThermalBoundary b = (NeumannThermalBoundary) heatBoundary;
			leftThermalBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.LEFT)));
			rightThermalBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.RIGHT)));
			upperThermalBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.UPPER)));
			lowerThermalBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.LOWER)));
			leftThermalBoundaryLabel.setText("Left boundary heat flux");
			rightThermalBoundaryLabel.setText("Right boundary heat flux");
			upperThermalBoundaryLabel.setText("Upper boundary heat flux");
			lowerThermalBoundaryLabel.setText("Lower boundary heat flux");
			leftThermalBoundaryLabel2.setText("<html><i>\u2103/m</i></html>");
			rightThermalBoundaryLabel2.setText("<html><i>\u2103/m</i></html>");
			upperThermalBoundaryLabel2.setText("<html><i>\u2103/m</i></html>");
			lowerThermalBoundaryLabel2.setText("<html><i>\u2103/m</i></html>");
		} else {
			enableBoundaryFieldsAndLabels(false);
		}
	}

	private void enableBoundaryFieldsAndLabels(boolean b) {
		leftThermalBoundaryField.setEnabled(b);
		rightThermalBoundaryField.setEnabled(b);
		upperThermalBoundaryField.setEnabled(b);
		lowerThermalBoundaryField.setEnabled(b);
		leftThermalBoundaryLabel.setEnabled(b);
		rightThermalBoundaryLabel.setEnabled(b);
		upperThermalBoundaryLabel.setEnabled(b);
		lowerThermalBoundaryLabel.setEnabled(b);
		leftThermalBoundaryLabel2.setEnabled(b);
		rightThermalBoundaryLabel2.setEnabled(b);
		upperThermalBoundaryLabel2.setEnabled(b);
		lowerThermalBoundaryLabel2.setEnabled(b);
	}

	private float parse(String s) {
		return MiscUtil.parse(owner, s);
	}

}