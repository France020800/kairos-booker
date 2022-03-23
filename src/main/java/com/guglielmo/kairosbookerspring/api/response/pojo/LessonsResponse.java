
package com.guglielmo.kairosbookerspring.api.response.pojo;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class LessonsResponse {

    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("sede")
    @Expose
    private String sede;
    @SerializedName("ora_inizio")
    @Expose
    private String oraInizio;
    @SerializedName("ora_fine")
    @Expose
    private String oraFine;
    @SerializedName("qr")
    @Expose
    private String qr;
    @SerializedName("prenotazioni")
    @Expose
    private List<Prenotazioni> prenotazioni = null;
    @SerializedName("timestamp")
    @Expose
    private Integer timestamp;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(String oraInizio) {
        this.oraInizio = oraInizio;
    }

    public String getOraFine() {
        return oraFine;
    }

    public void setOraFine(String oraFine) {
        this.oraFine = oraFine;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public List<Prenotazioni> getPrenotazioni() {
        return prenotazioni;
    }

    public void setPrenotazioni(List<Prenotazioni> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

}
