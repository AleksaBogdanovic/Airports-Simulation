package gui;

import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.Panel;

import project.Airport;
import systemdata.SystemData;

public class AirportListPanel extends Panel{
	
   
    
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

        for (Airport a : SystemData.getAirports()) {
            String tekst = a.getCode()
                    + "[" + a.getX() + ", " + a.getY() + "]";

            Checkbox checkbox = new Checkbox(tekst);
            checkbox.setState(SystemData.getCheckedAirports().contains(a));

            
            checkbox.addItemListener(e -> {
                if (checkbox.getState()) {
                    SystemData.addToCheckedAirport(a);
                } else {
                    SystemData.removeFromCheckedAirport(a);
                }
            	SystemData.resetActiveFlights();   
                if (onChangeCallback != null) {
                    onChangeCallback.run();
                }
            });

            
            add(checkbox);
        }

        validate(); //force refresf
	}
	

}
