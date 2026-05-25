package observer;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author msand
 */
public class ModelEventBus {
 
    private static ModelEventBus instance;
 
    public static ModelEventBus getInstance() {
        if (instance == null) {
            instance = new ModelEventBus();
        }
        return instance;
    }
 
    private ModelEventBus() {
        this.observers = new ArrayList<>();
    }
 
    private final List<Observer> observers;
 
    public void subscribe(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }
 
    public void publish(ModelEvent event) {

        List<Observer> copia = new ArrayList<>(observers);
        for (Observer observer : copia) {
            observer.onModelChanged(event);
        }
    }
}
