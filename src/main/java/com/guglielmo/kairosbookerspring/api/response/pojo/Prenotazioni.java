
package com.guglielmo.kairosbookerspring.api.response.pojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.ToString;

@Generated("jsonschema2pojo")
@ToString
public class Prenotazioni {

    @SerializedName("nome")
    @Expose
    private String nome;
    @SerializedName("ora_inizio")
    @Expose
    private String oraInizio;
    @SerializedName("ora_fine")
    @Expose
    private String oraFine;
    @SerializedName("aula")
    @Expose
    private String aula;
    @SerializedName("entry_id")
    @Expose
    private Integer entryId;
    @SerializedName("last_minute")
    @Expose
    private Boolean lastMinute;
    @SerializedName("capacita")
    @Expose
    private Integer capacita;
    @SerializedName("presenti")
    @Expose
    private Integer presenti;
    @SerializedName("prenotabile")
    @Expose
    private Boolean prenotabile;
    @SerializedName("note")
    @Expose
    private String note;
    @SerializedName("prenotata")
    @Expose
    private Boolean prenotata;
    @SerializedName("posto")
    @Expose
    private Integer posto;
    @SerializedName("PresenzaAula")
    @Expose
    private Object presenzaAula;
    @SerializedName("PostoOccupato")
    @Expose
    private Object postoOccupato;
    @SerializedName("Autocertificabile")
    @Expose
    private Integer autocertificabile;
    @SerializedName("Accompagnatore")
    @Expose
    private Integer accompagnatore;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public Integer getEntryId() {
        return entryId;
    }

    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }

    public Boolean getLastMinute() {
        return lastMinute;
    }

    public void setLastMinute(Boolean lastMinute) {
        this.lastMinute = lastMinute;
    }

    public Integer getCapacita() {
        return capacita;
    }

    public void setCapacita(Integer capacita) {
        this.capacita = capacita;
    }

    public Integer getPresenti() {
        return presenti;
    }

    public void setPresenti(Integer presenti) {
        this.presenti = presenti;
    }

    public Boolean getPrenotabile() {
        return prenotabile;
    }

    public void setPrenotabile(Boolean prenotabile) {
        this.prenotabile = prenotabile;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getPrenotata() {
        return prenotata;
    }

    public void setPrenotata(Boolean prenotata) {
        this.prenotata = prenotata;
    }

    public Integer getPosto() {
        return posto;
    }

    public void setPosto(Integer posto) {
        this.posto = posto;
    }

    public Object getPresenzaAula() {
        return presenzaAula;
    }

    public void setPresenzaAula(Object presenzaAula) {
        this.presenzaAula = presenzaAula;
    }

    public Object getPostoOccupato() {
        return postoOccupato;
    }

    public void setPostoOccupato(Object postoOccupato) {
        this.postoOccupato = postoOccupato;
    }

    public Integer getAutocertificabile() {
        return autocertificabile;
    }

    public void setAutocertificabile(Integer autocertificabile) {
        this.autocertificabile = autocertificabile;
    }

    public Integer getAccompagnatore() {
        return accompagnatore;
    }

    public void setAccompagnatore(Integer accompagnatore) {
        this.accompagnatore = accompagnatore;
    }

}
