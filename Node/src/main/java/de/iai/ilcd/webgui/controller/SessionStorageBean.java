package de.iai.ilcd.webgui.controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.Hashtable;


/**
 * If something needs to be stored beyond the scope of the bean, this is the place.
 * <p>
 * Deposits can only be retrieved once.
 * <p>
 * Make sure you're key is unique.
 */
@ManagedBean(name = "sessionStore")
@SessionScoped
public class SessionStorageBean implements Serializable {

    private static final long serialVersionUID = -5021229977502848575L;

    private final Store<String, FacesMessage> messageStore = new Store<>();

    public void depositMessage(String key, FacesMessage msg) {
        messageStore.deposit(key, msg);
    }

    public FacesMessage retrieveMessage(String key) {
        return messageStore.retrieve(key);
    }

    /**
     * A wrapper for Hashtable.<br/>
     * The difference: Entries can only be retrieved once.
     *
     * @param <T> Type of the keys
     * @param <U> Type of the values
     */
    private class Store<T, U> {

        Hashtable<T, U> table = new Hashtable<>();

        void deposit(T key, U value) {
            table.put(key, value);
        }

        U retrieve(T key) {
            U value = table.get(key);
            table.remove(key);
            return value;
        }

    }

}
