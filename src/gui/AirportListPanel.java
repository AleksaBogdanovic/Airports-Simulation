package gui;

import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.Panel;
import java.util.ArrayList;

import project.Airport;
import systemdata.AirportRegistry;

public class AirportListPanel extends Panel{
	
    private final ArrayList<Airport> checked = new ArrayList<Airport>();
    
    private Runnable onChangeCallback;

    public void setOnChangeCallback(Runnable callback) {
        this.onChangeCallback = callback;
    }

	public AirportListPanel() {
		setLayout(new GridLayout(0, 1));
        refresh();
        
        this.setSize(400,100);
        this.setVisible(true);
	}
	
	public void refresh() {
		removeAll();

        for (Airport a : AirportRegistry.all()) {
            String tekst = a.getCode()
                    + "[" + a.getX() + ", " + a.getY() + "]";

            Checkbox checkbox = new Checkbox(tekst);
            checkbox.setState(checked.contains(a));

            
            checkbox.addItemListener(e -> {
                if (checkbox.getState()) {
                    checked.add(a);
                } else {
                    checked.remove(a);
                }
            	//SystemData.resetActiveFlights();   
                if (onChangeCallback != null) {
                    onChangeCallback.run();
                }
            });

            
            add(checkbox);
        }

        validate(); //force refresf
	}
	

	public ArrayList<Airport> getCheckedAirports(){
		return this.checked;
	}
}
