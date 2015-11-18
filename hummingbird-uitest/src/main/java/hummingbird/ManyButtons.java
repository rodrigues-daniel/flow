package hummingbird;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

public class ManyButtons extends UI {

    @Override
    protected void init(VaadinRequest request) {
        for (int i = 0; i < 10; i++) {
            final int j = i;
            Button b = new Button("Button " + i);
            b.addClickListener(e -> {
                System.out.println("Click on button " + j);
            });
            addComponent(b);
        }

    }

}