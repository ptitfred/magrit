package org.kercoin.magrit.utils;

public class CommitterIdentity {
	private final String email;
	private final String name;
	private final String toString;

	public CommitterIdentity(String email, String name) {
		super();
		this.email = email;
		this.name = name;
		this.toString = String.format("\"%s\" <%s>", name, email);
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return toString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((toString == null) ? 0 : toString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommitterIdentity other = (CommitterIdentity) obj;
		if (toString == null) {
			if (other.toString != null)
				return false;
		} else if (!toString.equals(other.toString))
			return false;
		return true;
	}
	
	
}
