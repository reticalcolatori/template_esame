package model;

public class Viaggio {

	private String id;
	private String tipo; //arrivo/partenza
	private String partenza; //citta di partenza
	private String arrivo; //citta di arrivo
	
	//ora attesa nel formato hh:mm
	private int hh;
	private int mm;
	
	//ritardo
	private int ritardo;
	
	//stringa che rappresenta il nome del file audio
	private String fileAudio;

	public Viaggio(String id, String tipo, String partenza, String arrivo, int hh, int mm, int ritardo,
			String fileAudio) {
		this.id = id;
		this.tipo = tipo;
		this.partenza = partenza;
		this.arrivo = arrivo;
		this.hh = hh;
		this.mm = mm;
		this.ritardo = ritardo;
		this.fileAudio = fileAudio;
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

	public String getPartenza() {
		return partenza;
	}

	public void setPartenza(String partenza) {
		this.partenza = partenza;
	}

	public String getArrivo() {
		return arrivo;
	}

	public void setArrivo(String arrivo) {
		this.arrivo = arrivo;
	}

	public int getHh() {
		return hh;
	}

	public void setHh(int hh) {
		this.hh = hh;
	}

	public int getMm() {
		return mm;
	}

	public void setMm(int mm) {
		this.mm = mm;
	}

	public int getRitardo() {
		return ritardo;
	}

	public void setRitardo(int ritardo) {
		this.ritardo = ritardo;
	}

	public String getFileAudio() {
		return fileAudio;
	}

	public void setFileAudio(String fileAudio) {
		this.fileAudio = fileAudio;
	}
	
	
}
