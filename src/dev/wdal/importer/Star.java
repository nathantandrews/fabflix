package dev.wdal.importer;

public class Star {

	private String id;

	private StarKey key;

	public Star(){
		key = new StarKey();
	}
	
	public Star(String id, StarKey key) {
		this.id = id;
		this.key = key;
	}

	public StarKey getKey() {
		return key;
	}

	public int getDob() {
		return key.getDob();
	}

	public void setDob(int dob) {
		key.setDob(dob);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return key.getName();
	}

	public void setName(String name) {
		key.setName(name);
	}

	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		sb.append("'" + getId() + "'");
		sb.append(",");
		sb.append("'" + getName() + "'");
		sb.append(",");
		if (getDob() == 0) {
			sb.append("\\N");
		}
		else {
			sb.append(getDob());
		}
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (getId() != null && !getId().isBlank()) {
			sb.append("{" + getId() + "} ");
		}
		sb.append(key);
		return sb.toString();
	}
}
