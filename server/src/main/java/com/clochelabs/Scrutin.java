package com.clochelabs;

import com.clochelabs.packet.*;


import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Scrutin {
    private int id;
    private final String choix1;
    private final String choix2;
    private final String intitule;
    private Date dateFin;
    private final Date dateDebut;

    private ArrayList<Integer> idVotants;

    private BigInteger[] agregat;
    private PublicKey key;

    private String result;

    private boolean hasBeenClosed;

    public Scrutin(String intitule, String choix1, String choix2, Date dateFin){
        this.intitule = intitule;
        this.choix1 = choix1;
        this.choix2 = choix2;
        dateDebut = dateFin;
        this.dateFin = dateFin;
        idVotants = new ArrayList<>();
        this.id = Urne.getInstance().newId();
        requestPublicKey();
        agregat = Crypto.Encrypt(key,0);
        result ="";
        hasBeenClosed = false;
    }

    private void requestPublicKey(){
        System.out.println("envoie socket");
        System.out.println("envoyé");

        Packet get = Sender.send(new GetKeyPacket(id));
        System.out.println("reçu");
        switch (get.getType()){
            case ERROR -> {
                System.out.println("erreur : "+ ((ErrorPacket) get).getMessage());
            }
            case GIVEKEY -> {
                key = ((GiveKeyPacket) get).getPk();
                System.out.println(key);
            }
        }
    }



    public BigInteger[] getAgregat() {
        return agregat;
    }


    public boolean addVote(BigInteger[] clientVote, Integer idVotant){
        if(!this.idVotants.contains(idVotant)){
            agregat = Crypto.Agrege(agregat,clientVote,key.getP());
            idVotants.add(idVotant);
            return true;
        }
        return false;
    }

    public void setKey(PublicKey key) {
        this.key = key;
    }

    public int getNbVotants(){
        return idVotants.size();
    }

    public void setAgregat(BigInteger[] agregat) {
        this.agregat = agregat;
    }

    public PublicKey getKey() {
        return key;
    }

    @Override
    public String toString(){
        return intitule + "," + choix1 + "," + choix2 + "," + dateDebut.toString() + "," + dateFin.toString() + ".";
    }

    public boolean isHasBeenClosed() {
        return hasBeenClosed;
    }

    public boolean isEnded(){
        if((new Date()).compareTo(dateFin) > 0){
            hasBeenClosed = true;
            return true;
        }
        return false;
    }


    public void close(){
        int resultDecrypt = decryptResult();
        System.out.println(resultDecrypt);
        if(getNbVotants() == 0){
            result = "pas de votants";
        }else{
            if((float) (resultDecrypt / getNbVotants()) > 0.5) {
                result = choix1;
            }else{
                result = choix2;
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        dateFin = cal.getTime();
    }

    public int decryptResult(){
        GiveResultScrutPacket get = (GiveResultScrutPacket) Sender.send(new GetResultPacket(id,agregat));
        return get.getResult();
    }

    public int getIdScrutin() {
        return id;
    }



    public ScrutinDataObject getDataObject(){
        return new ScrutinDataObject(id,choix1, choix2, intitule, DateUtils.dateToString(dateFin), DateUtils.dateToString(dateDebut));
    }
}
