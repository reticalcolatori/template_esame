package model;

public class Calza {

	private String id, tipo, citta, via, messaggio;
	private char carbone;

	public Calza(String id, String tipo, String citta, String via, String messaggio, char carbone) {
		super();
		this.id = id;
		this.tipo = tipo;
		this.citta = citta;
		this.via = via;
		this.messaggio = messaggio;
		this.carbone = carbone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCitta() {
		return citta;
	}

	public void setCitta(String citta) {
		this.citta = citta;
	}

	public String getVia() {
		return via;
	}

	public void setVia(String via) {
		this.via = via;
	}

	public String getMessaggio() {
		return messaggio;
	}

	public void setMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}

	public char getCarbone() {
		return carbone;
	}

	public void setCarbone(char carbone) {
		this.carbone = carbone;
	}	
	
}
