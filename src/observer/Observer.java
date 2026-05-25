package observer;

/**
 *
 * @author msand
 */
public interface Observer {
    
    void onModelChanged(ModelEvent event);
}
