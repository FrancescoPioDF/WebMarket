/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

/**
 * Messaggio di feedback da mostrare all'utente dopo un'operazione.
 * Il tipo determina lo stile con cui viene reso nel template.
 */
public class Message {

    private String type;
    private String content;

    public Message(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public static Message success(String content) {
        return new Message("success", content);
    }

    public static Message error(String content) {
        return new Message("error", content);
    }

    //getter e setter

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}