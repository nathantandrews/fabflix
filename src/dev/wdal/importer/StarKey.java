package dev.wdal.importer;

public class StarKey {

	private String name;
	private int dob;

	public StarKey(){
		
	}
	
	public StarKey(String name, int dob) {
		this.name = name;
		this.dob  = dob;
	}
	public int getDob() {
		return dob;
	}

	public void setDob(int dob) {
		this.dob = dob;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (name == null) {
			sb.append("{null}");
		}
		else if (name.isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(getName());
		}
		sb.append(" (");
		sb.append(getDob());
		sb.append(") ");
		return sb.toString();
	}
}
