package org.energy2d.view;

import javax.swing.JDialog;

import org.energy2d.model.Anemometer;
import org.energy2d.model.Cloud;
import org.energy2d.model.Fan;
import org.energy2d.model.HeatFluxSensor;
import org.energy2d.model.Heliostat;
import org.energy2d.model.Model2D;
import org.energy2d.model.Part;
import org.energy2d.model.Particle;
import org.energy2d.model.ParticleFeeder;
import org.energy2d.model.Thermometer;
import org.energy2d.model.Tree;

/**
 * @author Charles Xie
 * 
 */
final class DialogFactory {

	private View2D view;
	private boolean modal = true;

	DialogFactory(View2D view) {
		this.view = view;
	}

	void setModal(boolean modal) {
		this.modal = modal;
	}

	JDialog createModelDialog(Object o) {
		if (o instanceof Model2D)
			return new ModelDialog(view, (Model2D) o, modal);
		if (o instanceof Part)
			return new PartModelDialog(view, (Part) o, modal);
		if (o instanceof Particle)
			return new ParticleDialog(view, (Particle) o, modal);
		if (o instanceof Cloud)
			return new CloudDialog(view, (Cloud) o, modal);
		if (o instanceof Tree)
			return new TreeDialog(view, (Tree) o, modal);
		if (o instanceof Fan)
			return new FanDialog(view, (Fan) o, modal);
		if (o instanceof Heliostat)
			return new HeliostatDialog(view, (Heliostat) o, modal);
		if (o instanceof ParticleFeeder)
			return new ParticleFeederDialog(view, (ParticleFeeder) o, modal);
		if (o instanceof Thermometer)
			return new ThermometerDialog(view, (Thermometer) o, modal);
		if (o instanceof Anemometer)
			return new AnemometerDialog(view, (Anemometer) o, modal);
		if (o instanceof HeatFluxSensor)
			return new HeatFluxSensorDialog(view, (HeatFluxSensor) o, modal);
		if (o instanceof TextBox)
			return new TextBoxPanel((TextBox) o, view).createDialog(modal);
		if (o instanceof Picture)
			return new PictureDialog(view, (Picture) o, modal);
		return null;
	}

	JDialog createViewDialog(Object o) {
		if (o instanceof View2D)
			return new ViewDialog(view, modal);
		if (o instanceof Part)
			return new PartViewDialog(view, (Part) o, modal);
		if (o instanceof Particle)
			return new ParticleDialog(view, (Particle) o, modal);
		if (o instanceof Cloud)
			return new CloudDialog(view, (Cloud) o, modal);
		if (o instanceof Tree)
			return new TreeDialog(view, (Tree) o, modal);
		if (o instanceof Fan)
			return new FanDialog(view, (Fan) o, modal);
		if (o instanceof Heliostat)
			return new HeliostatDialog(view, (Heliostat) o, modal);
		if (o instanceof ParticleFeeder)
			return new ParticleFeederDialog(view, (ParticleFeeder) o, modal);
		if (o instanceof Thermometer)
			return new ThermometerDialog(view, (Thermometer) o, modal);
		if (o instanceof Anemometer)
			return new AnemometerDialog(view, (Anemometer) o, modal);
		if (o instanceof HeatFluxSensor)
			return new HeatFluxSensorDialog(view, (HeatFluxSensor) o, modal);
		if (o instanceof TextBox)
			return new TextBoxPanel((TextBox) o, view).createDialog(modal);
		return null;
	}

}
